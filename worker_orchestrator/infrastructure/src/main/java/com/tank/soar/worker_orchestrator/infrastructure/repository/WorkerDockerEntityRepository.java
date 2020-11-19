package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import io.quarkus.agroal.DataSource;
import io.agroal.api.AgroalDataSource;
import org.apache.commons.lang3.Validate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.json.Json;
import javax.json.JsonValue;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class WorkerDockerEntityRepository implements WorkerRepository {

    private static final String HAS_WORKER = "SELECT EXISTS(SELECT 1 FROM WORKER WHERE workerId = ?)";

    private static final String CREATE_WORKER = "INSERT INTO WORKER (workerId, script, workerStatus, createdAt, lastUpdateStateDate, zoneOffset) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_WORKER = "UPDATE WORKER SET workerStatus = ?, lastUpdateStateDate = ?, createdAt = ?, zoneOffset = ?, container = to_json(?::json), logStreams = to_json(?::json) " +
            "WHERE workerId = ?";

    private static final String LIST_ALL_WORKERS = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt, zoneOffset FROM WORKER";
    private static final String GET_WORKER = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt, zoneOffset FROM WORKER WHERE workerId = ?";

    private final AgroalDataSource workerDataSource;
    private final WorkerLockMechanism workerLockMechanism;

    public WorkerDockerEntityRepository(@DataSource("workers") final AgroalDataSource workerDataSource,
                                        @Any final WorkerLockMechanism workerLockMechanism) {
        this.workerDataSource = Objects.requireNonNull(workerDataSource);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
    }

    @Override
    public WorkerId createWorker(final WorkerId workerId, final String script,
                                 final UTCZonedDateTime createdAt, final UTCZonedDateTime lastUpdateStateDate) {
        workerLockMechanism.lock(workerId);
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement createWorkerPreparedStatement = connection.prepareStatement(CREATE_WORKER)) {
            createWorkerPreparedStatement.setString(1, workerId.id());
            createWorkerPreparedStatement.setString(2, script);
            createWorkerPreparedStatement.setString(3, WorkerStatus.CREATING.name());
            createWorkerPreparedStatement.setObject(4, createdAt.localDateTime());
            createWorkerPreparedStatement.setObject(5, lastUpdateStateDate.localDateTime());
            createWorkerPreparedStatement.setObject(6, createdAt.zoneOffset().getId());
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
    public Worker saveWorker(final Worker worker,
                             final ContainerInformation containerInformation,
                             final List<? extends LogStream> logStreams) {
        final WorkerId workerId = worker.workerId();
        final String logStreamsAsJsonString = new StringBuilder("[")
                .append(
                        logStreams.stream()
                                .map(LogStream::toJsonStringRepresentation)
                                .collect(Collectors.joining(",")))
                .append("]")
                .toString();
        workerLockMechanism.lock(workerId);
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement saveWorkerPreparedStatement = connection.prepareStatement(UPDATE_WORKER)) {
            saveWorkerPreparedStatement.setString(1, worker.workerStatus().name());
            saveWorkerPreparedStatement.setObject(2, worker.lastUpdateStateDate().localDateTime());
            saveWorkerPreparedStatement.setObject(3, worker.createdAt().localDateTime());
            saveWorkerPreparedStatement.setString(4, worker.createdAt().zoneOffset().getId());
            saveWorkerPreparedStatement.setString(5, containerInformation.fullInformation());
            saveWorkerPreparedStatement.setString(6, logStreamsAsJsonString);
            saveWorkerPreparedStatement.setString(7, workerId.id());
            final int updated = saveWorkerPreparedStatement.executeUpdate();
            Validate.validState(updated == 1);
            return worker;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        } finally {
            workerLockMechanism.unlock(workerId);
        }
    }

    @Override
    public Boolean hasWorker(final WorkerId workerId) {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement hasWorkerPreparedStatement = connection.prepareStatement(HAS_WORKER)) {
            hasWorkerPreparedStatement.setString(1, workerId.id());
            final ResultSet resultSet = hasWorkerPreparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends Worker> listAllWorkers() {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(LIST_ALL_WORKERS)) {
            final List<WorkerDockerEntity> workerDockerEntities = new ArrayList<>();
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    workerDockerEntities.add(new WorkerDockerEntity(resultSet));
                }
                return workerDockerEntities;
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
                if (resultSet.next()) {
                    return new WorkerDockerEntity(resultSet);
                } else {
                    throw new UnknownWorkerException(workerId);
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
        final StringBuilder queryBuilder = new StringBuilder("SELECT workerId, array_to_json(array_agg(l)) FROM WORKER w, json_array_elements(w.logStreams::json) l WHERE ");
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

}
