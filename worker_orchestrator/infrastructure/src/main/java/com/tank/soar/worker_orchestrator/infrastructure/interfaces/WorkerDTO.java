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

    public WorkerDTO(final Worker worker) {
        this.workerId = worker.workerId().id();
        this.workerStatus = worker.workerStatus();
        this.lastUpdateStateDate = worker.lastUpdateStateDate();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDTO)) return false;
        WorkerDTO workerDTO = (WorkerDTO) o;
        return Objects.equals(workerId, workerDTO.workerId) &&
                workerStatus == workerDTO.workerStatus &&
                Objects.equals(lastUpdateStateDate, workerDTO.lastUpdateStateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, lastUpdateStateDate);
    }

    @Override
    public String toString() {
        return "WorkerDTO{" +
                "workerId='" + workerId + '\'' +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                '}';
    }
}
