package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public final class NewDockerContainerWorker implements Worker {

    private final WorkerId workerId;
    private final WorkerStatus workerStatus;
    private final Source source;
    private final UTCZonedDateTime lastUpdateStateDate;
    private final UTCZonedDateTime createdAt;

    private NewDockerContainerWorker(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.workerStatus = Objects.requireNonNull(builder.workerStatus);
        this.source = Objects.requireNonNull(builder.source);
        this.lastUpdateStateDate = Objects.requireNonNull(builder.lastUpdateStateDate);
        this.createdAt = Objects.requireNonNull(builder.createdAt);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private WorkerId workerId;
        private WorkerStatus workerStatus;
        private Source source;
        private UTCZonedDateTime lastUpdateStateDate;
        private UTCZonedDateTime createdAt;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withWorkerStatus(final WorkerStatus workerStatus) {
            this.workerStatus = workerStatus;
            return this;
        }

        public Builder withSource(final Source source) {
            this.source = source;
            return this;
        }

        public Builder withLastUpdateStateDate(final UTCZonedDateTime lastUpdateStateDate) {
            this.lastUpdateStateDate = lastUpdateStateDate;
            return this;
        }

        public Builder withCreatedAt(final UTCZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NewDockerContainerWorker build() {
            return new NewDockerContainerWorker(this);
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
    public UTCZonedDateTime createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewDockerContainerWorker)) return false;
        NewDockerContainerWorker that = (NewDockerContainerWorker) o;
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
        return "NewDockerContainerWorker{" +
                "workerId=" + workerId +
                ", workerStatus=" + workerStatus +
                ", source=" + source +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
