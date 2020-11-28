package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import com.tank.soar.worker_orchestrator.infrastructure.container.NewWorkerDockerEvent;
import io.quarkus.agroal.DataSource;
import io.agroal.api.AgroalDataSource;
import org.apache.commons.lang3.Validate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.json.Json;
import javax.json.JsonValue;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class WorkerDockerEntityRepository implements WorkerRepository {

    private static final String CREATE_WORKER = "INSERT INTO WORKER (workerId, script) VALUES (?, ?)";

    private static final String LIST_ALL_WORKERS = "SELECT w.workerId AS workerId, " +
            "s.workerId AS workerEventWorkerId, " +
            "s.eventType AS workerEventEventType, " +
            "s.eventDate AS workerEventEventDate, " +
            "s.zoneOffset AS workerEventZoneOffset, " +
            "s.container -> 'State' ->> 'Status' AS workerEventDockerStatus, " +
            "s.userEventType AS workerEventUserEventType " +
            "FROM WORKER w JOIN WORKER_EVENT s ON s.workerId = w.workerId";

    private static final String GET_WORKER = LIST_ALL_WORKERS + " WHERE w.workerId = ?";

    public static final String CREATE_USER_WORKER_EVENT = "INSERT INTO WORKER_EVENT " +
            "(workerId, userEventType, eventDate, zoneOffset, eventType) " +
            "VALUES (?, ?, ?, ?, 'USER')";

    private static final String CREATE_DOCKER_WORKER_EVENT = "INSERT INTO WORKER_EVENT " +
            "(workerId, container, logStreams, eventDate, zoneOffset, eventType) " +
            "VALUES (?, to_json(?::json), to_json(?::json), ?, ?, 'DOCKER')";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private final AgroalDataSource workerDataSource;
    private final WorkerLockMechanism workerLockMechanism;
    private final Event<NewWorkerEntityEvent> newWorkerEntityEvent;

    public WorkerDockerEntityRepository(@DataSource("workers") final AgroalDataSource workerDataSource,
                                        @Any final WorkerLockMechanism workerLockMechanism,
                                        final Event<NewWorkerEntityEvent> newWorkerEntityEvent) {
        this.workerDataSource = Objects.requireNonNull(workerDataSource);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
        this.newWorkerEntityEvent = Objects.requireNonNull(newWorkerEntityEvent);
    }

    @Override
    public WorkerId createWorker(final WorkerId workerId,
                                 final String script,
                                 final UTCZonedDateTime createdAt) {
        workerLockMechanism.lock(workerId);
        final UserEventType userEventType = UserEventType.CREATION_REQUESTED;
        try (final Connection connection = workerDataSource.getConnection()) {
            try (final PreparedStatement createWorkerPreparedStatement = connection.prepareStatement(CREATE_WORKER);
                 final PreparedStatement createUserWorkerEventPreparedStatement = connection.prepareStatement(CREATE_USER_WORKER_EVENT)) {
                connection.setAutoCommit(false);
                createWorkerPreparedStatement.setString(1, workerId.id());
                createWorkerPreparedStatement.setString(2, script);
                Validate.validState(createWorkerPreparedStatement.executeUpdate() == 1);
                createUserWorkerEventPreparedStatement.setString(1, workerId.id());
                createUserWorkerEventPreparedStatement.setString(2, userEventType.name());
                createUserWorkerEventPreparedStatement.setObject(3, createdAt.localDateTime());
                createUserWorkerEventPreparedStatement.setString(4, createdAt.zoneOffset().getId());
                Validate.validState(createUserWorkerEventPreparedStatement.executeUpdate() == 1);
                connection.commit();
            } catch (final SQLException sqlException) {
                try {
                    connection.rollback();
                } catch (final SQLException sqlException1) {
                    throw new RuntimeException(sqlException1);
                }
                throw new RuntimeException(sqlException);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (final SQLException sqlException) {
                    throw new RuntimeException(sqlException);
                }
            }
        } catch (final SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            workerLockMechanism.unlock(workerId);
        }
        newWorkerEntityEvent.fire(NewWorkerEntityEvent.newBuilder()
                .withWorkerId(workerId)
                .withUserEventType(userEventType)
                .withEventDate(createdAt)
                .withEventType(EventType.USER).build());
        return workerId;
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
                                    .withWorkerId(workerId));
                    builder.withWorkerEventEntity(new WorkerEventEntity(resultSet));
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
                final WorkerDockerEntity.Builder builder = WorkerDockerEntity.newBuilder()
                        .withWorkerId(workerId);
                Boolean hasFoundIt = Boolean.FALSE;
                while (resultSet.next()) {
                    hasFoundIt = Boolean.TRUE;
                    Validate.validState(workerId.id().equals(resultSet.getString("workerId")));
                    builder.withWorkerEventEntity(new WorkerEventEntity(resultSet));
                }
                if (!hasFoundIt) {
                    throw new UnknownWorkerException(workerId);
                } else {
                    return builder.build();
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
        final StringBuilder queryBuilder = new StringBuilder("SELECT workerId, array_to_json(array_agg(l)) FROM WORKER_EVENT w, json_array_elements(w.logStreams::json) l WHERE ");
        final String whereLogStreamTypes = Stream.of(Boolean.TRUE.equals(stdOut) ? LogStreamType.STDOUT.name() : null,
                Boolean.TRUE.equals(stdErr) ? LogStreamType.STDERR.name() : null)
                .filter(Objects::nonNull)
                .map(value -> String.format("l->>'logStreamType' = '%s'", value))
                .collect(Collectors.joining(" OR "));
        final String query = queryBuilder
                .append("(")
                .append(whereLogStreamTypes)
                .append(") AND workerId = ? AND eventType = 'DOCKER' GROUP BY workerId, eventType, eventDate ORDER BY eventDate DESC LIMIT 1").toString();
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

    void onDockerStateChanged(@Observes final NewWorkerDockerEvent newWorkerDockerEvent) {
        final String logStreamsAsJsonString = Optional.ofNullable(newWorkerDockerEvent.stdResponses())
                .map((stdResponses) -> new StringBuilder("[")
                        .append(
                                stdResponses
                                        .stream()
                                        .map(LogStream::toJsonStringRepresentation)
                                        .collect(Collectors.joining(",")))
                        .append("]")
                        .toString())
                .orElse(null);
        workerLockMechanism.lock(newWorkerDockerEvent.workerId());
        try {
            final String container = OBJECT_MAPPER.writeValueAsString(newWorkerDockerEvent.container());
            try (final Connection connection = workerDataSource.getConnection();
                 final PreparedStatement createDockerStateSnapshotPreparedStatement = connection.prepareStatement(CREATE_DOCKER_WORKER_EVENT)) {
                createDockerStateSnapshotPreparedStatement.setString(1, newWorkerDockerEvent.workerId().id());
                createDockerStateSnapshotPreparedStatement.setString(2, container);
                createDockerStateSnapshotPreparedStatement.setString(3, logStreamsAsJsonString);
                createDockerStateSnapshotPreparedStatement.setObject(4, newWorkerDockerEvent.dockerStateChangedDate().localDateTime());
                createDockerStateSnapshotPreparedStatement.setString(5, newWorkerDockerEvent.dockerStateChangedDate().zoneOffset().getId());
                final int created = createDockerStateSnapshotPreparedStatement.executeUpdate();
                Validate.validState(created == 1);
            } catch (final SQLException e) {
                throw new RuntimeException(e);// FIXME better handle it !
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);// FIXME better handle it !
        } finally {
            workerLockMechanism.unlock(newWorkerDockerEvent.workerId());
        }
    }

}
