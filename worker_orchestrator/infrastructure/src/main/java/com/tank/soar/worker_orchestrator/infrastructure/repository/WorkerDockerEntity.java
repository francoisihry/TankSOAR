package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.*;
import org.apache.commons.lang3.Validate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WorkerDockerEntity implements Worker {

    private final WorkerId workerId;
    private final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities;
    private final Source source;
    private final WorkerStatus workerStatus;
    private final UTCZonedDateTime lastUpdateStateDate;

    private WorkerDockerEntity(final Builder builder) {
        Validate.notNull(builder.workerId);
        Validate.notNull(builder.workerSnapshotDockerEntities);
        this.workerId = builder.workerId;
        this.workerSnapshotDockerEntities = builder.workerSnapshotDockerEntities
                .stream()
                .sorted(Comparator.comparing(DockerStateSnapshotEntity::snapshotDate))
                .collect(Collectors.toList());
        this.source = Source.DATABASE;
        this.workerStatus = this.workerSnapshotDockerEntities.stream()
                .reduce((first, second) -> second)
                .map(DockerStateSnapshotEntity::workerStatus)
                .orElse(WorkerStatus.CREATING);
        this.lastUpdateStateDate = this.workerSnapshotDockerEntities.stream()
                .reduce((first, second) -> second)
                .map(DockerStateSnapshotEntity::snapshotDate)
                .orElse(builder.createdAt);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private WorkerId workerId;
        private UTCZonedDateTime createdAt;
        private List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = new ArrayList<>();

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withCreatedAt(final UTCZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withWorkerSnapshotDockerEntity(final DockerStateSnapshotEntity dockerStateSnapshotEntity) {
            this.workerSnapshotDockerEntities.add(dockerStateSnapshotEntity);
            return this;
        }

        public Builder withWorkerSnapshotDockerEntities(final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities) {
            this.workerSnapshotDockerEntities = workerSnapshotDockerEntities;
            return this;
        }

        public WorkerDockerEntity build() {
            return new WorkerDockerEntity(this);
        }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDockerEntity)) return false;
        WorkerDockerEntity that = (WorkerDockerEntity) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(workerSnapshotDockerEntities, that.workerSnapshotDockerEntities) &&
                source == that.source &&
                workerStatus == that.workerStatus &&
                Objects.equals(lastUpdateStateDate, that.lastUpdateStateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerSnapshotDockerEntities, source, workerStatus, lastUpdateStateDate);
    }

    @Override
    public String toString() {
        return "WorkerDockerEntity{" +
                "workerId=" + workerId +
                ", workerSnapshotDockerEntities=" + workerSnapshotDockerEntities +
                ", source=" + source +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                '}';
    }
}
