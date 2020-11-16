package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.WorkerLog;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public final class WorkerLogDTO {

    private final String workerId;
    private final String log;
    private final Boolean hasFinishedProducingLog;

    public WorkerLogDTO(final WorkerLog workerLog) {
        this.workerId = workerLog.workerId().id();
        this.log = workerLog.log();
        this.hasFinishedProducingLog = workerLog.hasFinishedProducingLog();
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getLog() {
        return log;
    }

    public Boolean getHasFinishedProducingLog() {
        return hasFinishedProducingLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerLogDTO)) return false;
        WorkerLogDTO that = (WorkerLogDTO) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(log, that.log) &&
                Objects.equals(hasFinishedProducingLog, that.hasFinishedProducingLog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, log, hasFinishedProducingLog);
    }

    @Override
    public String toString() {
        return "WorkerLogDTO{" +
                "workerId='" + workerId + '\'' +
                ", log='" + log + '\'' +
                ", hasFinishedProducingLog=" + hasFinishedProducingLog +
                '}';
    }
}
