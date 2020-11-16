package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import io.quarkus.agroal.DataSource;
import io.agroal.api.AgroalDataSource;
import org.apache.commons.lang3.Validate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WorkerDockerEntityRepository implements WorkerRepository {

    private static final String HAS_WORKER = "SELECT EXISTS(SELECT 1 FROM WORKER WHERE workerId = ?)";

    private static final String CREATE_WORKER = "INSERT INTO WORKER (workerId, script, workerStatus, createdAt, lastUpdateStateDate) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_WORKER = "UPDATE WORKER SET workerStatus = ?, lastUpdateStateDate = ?, createdAt = ?, container = to_json(?::json), stdOut = ?, stdErr = ? " +
            "WHERE workerId = ?";

    private static final String LIST_ALL_WORKERS = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt FROM WORKER";
    private static final String GET_WORKER = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt FROM WORKER WHERE workerId = ?";
    private static final String GET_STD_OUT = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt, stdOut FROM WORKER WHERE workerId = ?";
    private static final String GET_STD_ERR = "SELECT workerId, workerStatus, lastUpdateStateDate, createdAt, stdErr FROM WORKER WHERE workerId = ?";

    private final AgroalDataSource workerDataSource;
    private final WorkerLockMechanism workerLockMechanism;

    public WorkerDockerEntityRepository(@DataSource("workers") final AgroalDataSource workerDataSource,
                                        @Any final WorkerLockMechanism workerLockMechanism) {
        this.workerDataSource = Objects.requireNonNull(workerDataSource);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
    }

    @Override
    public WorkerId createWorker(final WorkerId workerId, final String script,
                                 final LocalDateTime createdAt, final LocalDateTime lastUpdateStateDate) {
        workerLockMechanism.lock(workerId);
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement createWorkerPreparedStatement = connection.prepareStatement(CREATE_WORKER)) {
            createWorkerPreparedStatement.setString(1, workerId.id());
            createWorkerPreparedStatement.setString(2, script);
            createWorkerPreparedStatement.setString(3, WorkerStatus.CREATING.name());
            createWorkerPreparedStatement.setObject(4, createdAt);
            createWorkerPreparedStatement.setObject(5, lastUpdateStateDate);
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
                             final WorkerLog stdOut,
                             final WorkerLog stdErr) {
        final WorkerId workerId = worker.workerId();
        workerLockMechanism.lock(workerId);
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement saveWorkerPreparedStatement = connection.prepareStatement(UPDATE_WORKER)) {
            saveWorkerPreparedStatement.setString(1, worker.workerStatus().name());
            saveWorkerPreparedStatement.setObject(2, worker.lastUpdateStateDate());
            saveWorkerPreparedStatement.setObject(3, worker.createdAt());
            saveWorkerPreparedStatement.setString(4, containerInformation.fullInformation());
            saveWorkerPreparedStatement.setString(5, stdOut.log());
            saveWorkerPreparedStatement.setString(6, stdErr.log());
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

    @Override
    public WorkerLog getStdOut(final WorkerId workerId) throws UnknownWorkerException {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(GET_STD_OUT)) {
            preparedStatement.setString(1, workerId.id());
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final WorkerDockerEntity workerDockerEntity = new WorkerDockerEntity(resultSet);
                    return new WorkerLogDockerEntity(workerDockerEntity, resultSet.getString("stdOut"));
                } else {
                    throw new UnknownWorkerException(workerId);
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkerLog getStdErr(final WorkerId workerId) throws UnknownWorkerException {
        try (final Connection connection = workerDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(GET_STD_ERR)) {
            preparedStatement.setString(1, workerId.id());
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final WorkerDockerEntity workerDockerEntity = new WorkerDockerEntity(resultSet);
                    return new WorkerLogDockerEntity(workerDockerEntity, resultSet.getString("stdErr"));
                } else {
                    throw new UnknownWorkerException(workerId);
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
