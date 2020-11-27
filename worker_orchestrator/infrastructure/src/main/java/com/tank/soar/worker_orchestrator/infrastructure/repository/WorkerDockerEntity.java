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
    private final List<WorkerEventEntity> workerEventEntities;
    private final Source source;
    private final WorkerStatus workerStatus;
    private final UTCZonedDateTime lastUpdateStateDate;

    private WorkerDockerEntity(final Builder builder) {
        Validate.notNull(builder.workerId);
        Validate.notNull(builder.workerEventEntities);
        Validate.validState(builder.workerEventEntities.size() > 0);
        this.workerId = builder.workerId;
        this.workerEventEntities = builder.workerEventEntities
                .stream()
                .sorted(Comparator.comparing(WorkerEventEntity::eventDate))
                .collect(Collectors.toList());
        this.source = Source.DATABASE;
        this.workerStatus = this.workerEventEntities.stream()
                .reduce((first, second) -> second)
                .map(WorkerEventEntity::workerStatus)
                .get();
        this.lastUpdateStateDate = this.workerEventEntities.stream()
                .reduce((first, second) -> second)
                .map(WorkerEventEntity::eventDate)
                .get();
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private WorkerId workerId;
        private List<WorkerEventEntity> workerEventEntities = new ArrayList<>();

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withWorkerEventEntity(final WorkerEventEntity workerEventEntity) {
            this.workerEventEntities.add(workerEventEntity);
            return this;
        }

        public Builder withWorkerEventEntities(final List<WorkerEventEntity> workerEventEntities) {
            this.workerEventEntities = workerEventEntities;
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
                Objects.equals(workerEventEntities, that.workerEventEntities) &&
                source == that.source &&
                workerStatus == that.workerStatus &&
                Objects.equals(lastUpdateStateDate, that.lastUpdateStateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerEventEntities, source, workerStatus, lastUpdateStateDate);
    }

    @Override
    public String toString() {
        return "WorkerDockerEntity{" +
                "workerId=" + workerId +
                ", workerEventEntities=" + workerEventEntities +
                ", source=" + source +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                '}';
    }
}
