package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public final class WorkerDockerEntity implements Worker {

    private final WorkerId workerId;
    private final WorkerStatus workerStatus;
    private final Source source;
    private final UTCZonedDateTime lastUpdateStateDate;
    private final UTCZonedDateTime createdAt;

    public WorkerDockerEntity(final WorkerId workerId,
                              final WorkerStatus workerStatus,
                              final UTCZonedDateTime createdAt,
                              final UTCZonedDateTime lastUpdateStateDate) {
        this.workerId = Objects.requireNonNull(workerId);
        this.workerStatus = Objects.requireNonNull(workerStatus);
        this.source = Source.DATABASE;
        this.lastUpdateStateDate = Objects.requireNonNull(lastUpdateStateDate);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public WorkerDockerEntity(final ResultSet resultSet) throws SQLException {
        this(
                new WorkerId(resultSet.getString("workerId")),
                WorkerStatus.valueOf(resultSet.getString("workerStatus")),
                UTCZonedDateTime.of(resultSet.getObject("createdAt", LocalDateTime.class),
                        resultSet.getString("zoneOffset")),
                UTCZonedDateTime.of(resultSet.getObject("lastUpdateStateDate", LocalDateTime.class),
                        resultSet.getString("zoneOffset"))
        );
    }

    @Override
    public WorkerId workerId() {
        return workerId;
    }

    @Override
    public WorkerStatus workerStatus() {
        return workerStatus;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public UTCZonedDateTime lastUpdateStateDate() {
        return lastUpdateStateDate;
    }

    @Override
    public UTCZonedDateTime createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDockerEntity)) return false;
        WorkerDockerEntity that = (WorkerDockerEntity) o;
        return Objects.equals(workerId, that.workerId) &&
                workerStatus == that.workerStatus &&
                source == that.source &&
                Objects.equals(lastUpdateStateDate, that.lastUpdateStateDate) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, source, lastUpdateStateDate, createdAt);
    }

    @Override
    public String toString() {
        return "WorkerDockerEntity{" +
                "workerId=" + workerId +
                ", workerStatus=" + workerStatus +
                ", source=" + source +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
