package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.ZonedDateTime;
import java.util.Objects;

@RegisterForReflection
public final class WorkerDTO {

    private final String workerId;
    private final WorkerStatus workerStatus;
    private final ZonedDateTime lastUpdateStateDate;
    private final Boolean hasFinished;

    public WorkerDTO(final Worker worker) {
        this.workerId = worker.workerId().id();
        this.workerStatus = worker.workerStatus();
        this.lastUpdateStateDate = worker.lastUpdateStateDate().zonedDateTime();
        this.hasFinished = worker.hasFinished();
    }

    public String getWorkerId() {
        return workerId;
    }

    public WorkerStatus getWorkerStatus() {
        return workerStatus;
    }

    public ZonedDateTime getLastUpdateStateDate() {
        return lastUpdateStateDate;
    }

    public Boolean getHasFinished() {
        return hasFinished;
    }

    public String toJsonStringRepresentation() {
        return String.format("{\"workerId\":\"%s\",\"workerStatus\":\"%s\",\"lastUpdateStateDate\":\"%s\", \"hasFinished\": \"%b\"}",
                workerId,
                workerStatus.name(),
                lastUpdateStateDate,
                hasFinished);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerDTO)) return false;
        WorkerDTO workerDTO = (WorkerDTO) o;
        return Objects.equals(workerId, workerDTO.workerId) &&
                workerStatus == workerDTO.workerStatus &&
                Objects.equals(lastUpdateStateDate, workerDTO.lastUpdateStateDate) &&
                Objects.equals(hasFinished, workerDTO.hasFinished);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, workerStatus, lastUpdateStateDate, hasFinished);
    }

    @Override
    public String toString() {
        return "WorkerDTO{" +
                "workerId='" + workerId + '\'' +
                ", workerStatus=" + workerStatus +
                ", lastUpdateStateDate=" + lastUpdateStateDate +
                ", hasFinished=" + hasFinished +
                '}';
    }
}
