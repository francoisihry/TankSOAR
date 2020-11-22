package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public final class WorkerDockerContainer implements Worker {

    private final WorkerId workerId;
    private final WorkerStatus workerStatus;
    private final UTCZonedDateTime lastUpdateStateDate;

    public WorkerDockerContainer(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.workerStatus = Objects.requireNonNull(builder.workerStatus);
        this.lastUpdateStateDate = Objects.requireNonNull(builder.lastUpdateStateDate);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private WorkerId workerId;
        private WorkerStatus workerStatus;
        private UTCZonedDateTime lastUpdateStateDate;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withWorkerStatus(final WorkerStatus workerStatus) {
            this.workerStatus = workerStatus;
            return this;
        }

        public Builder withLastUpdateStateDate(final UTCZonedDateTime lastUpdateStateDate) {
            this.lastUpdateStateDate = lastUpdateStateDate;
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
    public UTCZonedDateTime lastUpdateStateDate() {
        return lastUpdateStateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDockerContainer)) return false;
        WorkerDockerContainer that = (WorkerDockerContainer) o;
        return Objects.equals(workerId, that.workerId) &&
                workerStatus == that.workerStatus &&
                Objects.equals(lastUpdateStateDate, that.lastUpdateStateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, lastUpdateStateDate);
    }

    @Override
    public String toString() {
        return "WorkerDockerContainer{" +
                "workerId=" + workerId +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                '}';
    }
}
