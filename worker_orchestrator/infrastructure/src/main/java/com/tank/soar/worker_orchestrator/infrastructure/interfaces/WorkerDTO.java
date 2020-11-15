package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.util.Objects;

@RegisterForReflection
public final class WorkerDTO {

    private final String workerId;
    private final WorkerStatus workerStatus;
    private final LocalDateTime lastUpdateStateDate;
    private final LocalDateTime createdAt;
    private final Boolean hasFinished;

    public WorkerDTO(final Worker worker) {
        this.workerId = worker.workerId().id();
        this.workerStatus = worker.workerStatus();
        this.lastUpdateStateDate = worker.lastUpdateStateDate();
        this.createdAt = worker.createdAt();
        this.hasFinished = worker.hasFinished();
    }

    public String getWorkerId() {
        return workerId;
    }

    public WorkerStatus getWorkerStatus() {
        return workerStatus;
    }

    public LocalDateTime getLastUpdateStateDate() {
        return lastUpdateStateDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getHasFinished() {
        return hasFinished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDTO)) return false;
        WorkerDTO workerDTO = (WorkerDTO) o;
        return Objects.equals(workerId, workerDTO.workerId) &&
                workerStatus == workerDTO.workerStatus &&
                Objects.equals(lastUpdateStateDate, workerDTO.lastUpdateStateDate) &&
                Objects.equals(createdAt, workerDTO.createdAt) &&
                Objects.equals(hasFinished, workerDTO.hasFinished);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, lastUpdateStateDate, createdAt, hasFinished);
    }

    @Override
    public String toString() {
        return "WorkerDTO{" +
                "workerId='" + workerId + '\'' +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                ", createdAt=" + createdAt +
                ", hasFinished=" + hasFinished +
                '}';
    }
}
