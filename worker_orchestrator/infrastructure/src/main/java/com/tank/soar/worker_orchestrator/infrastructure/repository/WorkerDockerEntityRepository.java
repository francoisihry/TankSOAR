package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerStateChanged;
import io.quarkus.agroal.DataSource;
import io.agroal.api.AgroalDataSource;
import org.apache.commons.lang3.Validate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.json.Json;
import javax.json.JsonValue;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class WorkerDockerEntityRepository implements WorkerRepository {

    private static final String CREATE_WORKER = "INSERT INTO WORKER (workerId, script, createdAt, zoneOffset) VALUES (?, ?, ?, ?)";

    private static final String LIST_ALL_WORKERS = "SELECT w.createdAt AS createdAt, w.workerId AS workerId, w.zoneOffset AS workerZoneOffset, s.workerId AS snapshotWorkerId, s.container -> 'State' ->> 'Status' AS dockerStatus, s.snapshotDate AS snapshotDate, s.zoneOffset AS snapshotDateZoneOffset " +
            "FROM WORKER w LEFT JOIN DOCKER_STATE_SNAPSHOT s ON s.workerId = w.workerId";

    private static final String GET_WORKER = LIST_ALL_WORKERS + " WHERE w.workerId = ?";

    private static final String CREATE_DOCKER_STATE_SNAPSHOT = "INSERT INTO DOCKER_STATE_SNAPSHOT " +
            "(workerId, container, logStreams, snapshotDate, zoneOffset) " +
            "VALUES (?, to_json(?::json), to_json(?::json), ?, ?)";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AgroalDataSource workerDataSource;
    private final WorkerLockMechanism workerLockMechanism;

    public WorkerDockerEntityRepository(@DataSource("workers") final AgroalDataSource workerDataSource,
                                        @Any final WorkerLockMechanism workerLockMechanism) {
        this.workerDataSource = Objects.requireNonNull(workerDataSource);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
    }

    @Override
    public WorkerId createWorker(final WorkerId workerId,
                                 final String script,
                                 final UTCZonedDateTime createdAt) {
        workerLockMechanism.lock(workerId);
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement createWorkerPreparedStatement = connection.prepareStatement(CREATE_WORKER)) {
            createWorkerPreparedStatement.setString(1, workerId.id());
            createWorkerPreparedStatement.setString(2, script);
            createWorkerPreparedStatement.setObject(3, createdAt.localDateTime());
            createWorkerPreparedStatement.setObject(4, createdAt.zoneOffset().getId());
            final int created = createWorkerPreparedStatement.executeUpdate();
            Validate.validState(created == 1);
            return workerId;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        } finally {
            workerLockMechanism.unlock(workerId);
        }
    }

    @Override
    public List<? extends Worker> listAllWorkers() {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(LIST_ALL_WORKERS)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final Map<WorkerId, WorkerDockerEntity.Builder> workerIdBuilderMap = new HashMap<>();
                while (resultSet.next()) {
                    final WorkerId workerId = new WorkerId(resultSet.getString("workerId"));
                    final WorkerDockerEntity.Builder builder = workerIdBuilderMap
                            .getOrDefault(workerId, WorkerDockerEntity
                                    .newBuilder()
                                    .withWorkerId(workerId)
                                    .withCreatedAt(UTCZonedDateTime.of(
                                            resultSet.getObject("createdAt", LocalDateTime.class),
                                            resultSet.getString("workerZoneOffset"))));
                    if (resultSet.getString("snapshotWorkerId") != null) {
                        builder
                                .withWorkerSnapshotDockerEntity(new DockerStateSnapshotEntity(resultSet));
                    }
                    workerIdBuilderMap.put(workerId, builder);
                }
                return workerIdBuilderMap.values()
                        .stream()
                        .map(WorkerDockerEntity.Builder::build)
                        .collect(Collectors.toList());
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Worker getWorker(final WorkerId workerId) throws UnknownWorkerException {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(GET_WORKER)) {
            preparedStatement.setString(1, workerId.id());
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final WorkerDockerEntity.Builder builder = WorkerDockerEntity.newBuilder();
                final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = new ArrayList<>();
                Boolean hasFoundIt = Boolean.FALSE;
                while (resultSet.next()) {
                    hasFoundIt = Boolean.TRUE;
                    builder.withWorkerId(new WorkerId(resultSet.getString("workerId")))
                            .withCreatedAt(UTCZonedDateTime.of(
                                    resultSet.getObject("createdAt", LocalDateTime.class),
                                    resultSet.getString("workerZoneOffset")));
                    if (resultSet.getString("snapshotWorkerId") != null) {
                        workerSnapshotDockerEntities.add(new DockerStateSnapshotEntity(resultSet));
                    }
                }
                if (!hasFoundIt) {
                    throw new UnknownWorkerException(workerId);
                } else {
                    return builder
                            .withWorkerSnapshotDockerEntities(workerSnapshotDockerEntities)
                            .build();
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // https://stackoverflow.com/questions/24804348/how-to-filter-json-array-per-each-returned-row/24806907
    @Override
    public List<? extends LogStream> getLog(final WorkerId workerId,
                                            final Boolean stdOut,
                                            final Boolean stdErr) throws UnknownWorkerException {
        final StringBuilder queryBuilder = new StringBuilder("SELECT workerId, array_to_json(array_agg(l)) FROM DOCKER_STATE_SNAPSHOT w, json_array_elements(w.logStreams::json) l WHERE ");
        final String whereLogStreamTypes = Stream.of(Boolean.TRUE.equals(stdOut) ? LogStreamType.STDOUT.name() : null,
                Boolean.TRUE.equals(stdErr) ? LogStreamType.STDERR.name() : null)
                .filter(Objects::nonNull)
                .map(value -> String.format("l->>'logStreamType' = '%s'", value))
                .collect(Collectors.joining(" OR "));
        final String query = queryBuilder
                .append("(")
                .append(whereLogStreamTypes)
                .append(") AND workerId = ? GROUP BY workerId").toString();
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, workerId.id());
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // TODO je fais deux mappings... Optimisation : retourner un String wrapper dans une class porteuse de sens
                    return Json.createReader(new StringReader(resultSet.getString(2))).readArray()
                            .stream()
                            .map(JsonValue::asJsonObject)
                            .map(LogStreamEntity::new)
                            .collect(Collectors.toList());
                } else {
                    throw new UnknownWorkerException(workerId);
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void onDockerStateChanged(@Observes final DockerStateChanged dockerStateChanged) {
        final String logStreamsAsJsonString = Optional.ofNullable(dockerStateChanged.stdResponses())
                .map((stdResponses) -> new StringBuilder("[")
                        .append(
                                stdResponses
                                        .stream()
                                        .map(LogStream::toJsonStringRepresentation)
                                        .collect(Collectors.joining(",")))
                        .append("]")
                        .toString())
                .orElse(null);
        workerLockMechanism.lock(dockerStateChanged.workerId());
        try {
            final String container = OBJECT_MAPPER.writeValueAsString(dockerStateChanged.container());
            try (final Connection connection = workerDataSource.getConnection();
                 final PreparedStatement createDockerStateSnapshotPreparedStatement = connection.prepareStatement(CREATE_DOCKER_STATE_SNAPSHOT)) {
                createDockerStateSnapshotPreparedStatement.setString(1, dockerStateChanged.workerId().id());
                createDockerStateSnapshotPreparedStatement.setString(2, container);
                createDockerStateSnapshotPreparedStatement.setString(3, logStreamsAsJsonString);
                createDockerStateSnapshotPreparedStatement.setObject(4, dockerStateChanged.dockerStateChangedDate().localDateTime());
                createDockerStateSnapshotPreparedStatement.setString(5, dockerStateChanged.dockerStateChangedDate().zoneOffset().getId());
                final int created = createDockerStateSnapshotPreparedStatement.executeUpdate();
                Validate.validState(created == 1);
            } catch (final SQLException e) {
                throw new RuntimeException(e);// FIXME better handle it !
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);// FIXME better handle it !
        } finally {
            workerLockMechanism.unlock(dockerStateChanged.workerId());
        }
    }

}
