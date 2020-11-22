package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public final class DockerStateSnapshotEntity {

    private final WorkerId workerId;
    private final DockerContainerStatus dockerContainerStatus;
    private final UTCZonedDateTime snapshotDate;

    public DockerStateSnapshotEntity(final WorkerId workerId,
                                     final DockerContainerStatus dockerContainerStatus,
                                     final UTCZonedDateTime snapshotDate) {
        this.workerId = Objects.requireNonNull(workerId);
        this.dockerContainerStatus = Objects.requireNonNull(dockerContainerStatus);
        this.snapshotDate = Objects.requireNonNull(snapshotDate);
    }

    public DockerStateSnapshotEntity(final ResultSet resultSet) throws SQLException {
        this(
                new WorkerId(resultSet.getString("snapshotWorkerId")),
                DockerContainerStatus.fromDockerStatus(resultSet.getString("dockerStatus")),
                UTCZonedDateTime.of(resultSet.getObject("snapshotDate", LocalDateTime.class),
                        resultSet.getString("snapshotDateZoneOffset"))
        );
    }

    public WorkerId workerId() {
        return workerId;
    }

    public WorkerStatus workerStatus() {
        return dockerContainerStatus.toWorkerStatus();
    }

    public UTCZonedDateTime snapshotDate() {
        return snapshotDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerStateSnapshotEntity)) return false;
        DockerStateSnapshotEntity that = (DockerStateSnapshotEntity) o;
        return Objects.equals(workerId, that.workerId) &&
                dockerContainerStatus == that.dockerContainerStatus &&
                Objects.equals(snapshotDate, that.snapshotDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, dockerContainerStatus, snapshotDate);
    }

    @Override
    public String toString() {
        return "DockerStateSnapshotEntity{" +
                "workerId=" + workerId +
                ", dockerContainerStatus=" + dockerContainerStatus +
                ", snapshotDate=" + snapshotDate +
                '}';
    }
}
