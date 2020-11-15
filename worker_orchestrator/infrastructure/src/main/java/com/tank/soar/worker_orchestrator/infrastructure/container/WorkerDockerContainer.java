package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.Source;
import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public final class WorkerDockerContainer implements Worker {

    private final WorkerId workerId;
    private final WorkerStatus workerStatus;
    private final LocalDateTime lastUpdateStateDate;
    private final LocalDateTime createdAt;

    public WorkerDockerContainer(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.workerStatus = Objects.requireNonNull(builder.workerStatus);
        this.lastUpdateStateDate = Objects.requireNonNull(builder.lastUpdateStateDate);
        this.createdAt = Objects.requireNonNull(builder.createdAt);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private WorkerId workerId;
        private WorkerStatus workerStatus;
        private LocalDateTime lastUpdateStateDate;
        private LocalDateTime createdAt;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withWorkerStatus(final WorkerStatus workerStatus) {
            this.workerStatus = workerStatus;
            return this;
        }

        public Builder withLastUpdateStateDate(final LocalDateTime lastUpdateStateDate) {
            this.lastUpdateStateDate = lastUpdateStateDate;
            return this;
        }

        public Builder withCreatedAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorkerDockerContainer build() {
            return new WorkerDockerContainer(this);
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
        return Source.CONTAINER;
    }

    @Override
    public LocalDateTime lastUpdateStateDate() {
        return lastUpdateStateDate;
    }

    @Override
    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDockerContainer)) return false;
        WorkerDockerContainer that = (WorkerDockerContainer) o;
        return Objects.equals(workerId, that.workerId) &&
                workerStatus == that.workerStatus &&
                Objects.equals(lastUpdateStateDate, that.lastUpdateStateDate) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, lastUpdateStateDate, createdAt);
    }

    @Override
    public String toString() {
        return "WorkerDockerContainer{" +
                "workerId=" + workerId +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
