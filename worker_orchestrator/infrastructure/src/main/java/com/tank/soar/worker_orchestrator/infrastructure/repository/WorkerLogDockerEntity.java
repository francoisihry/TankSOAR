package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerLog;

import java.util.Objects;

public final class WorkerLogDockerEntity implements WorkerLog {

    private final WorkerId workerId;
    private final String log;
    private final Boolean hasFinishedProducingLog;

    public WorkerLogDockerEntity(final WorkerId workerId, final String log, final Boolean hasFinishedProducingLog) {
        this.workerId = Objects.requireNonNull(workerId);
        this.log = (log != null ? log : "");
        this.hasFinishedProducingLog = Objects.requireNonNull(hasFinishedProducingLog);
    }

    public WorkerLogDockerEntity(final WorkerDockerEntity workerDockerEntity, final String log) {
        this(workerDockerEntity.workerId(), log, workerDockerEntity.hasFinished());
    }

    @Override
    public WorkerId workerId() {
        return workerId;
    }

    @Override
    public String log() {
        return log;
    }

    @Override
    public Boolean hasFinishedProducingLog() {
        return hasFinishedProducingLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerLogDockerEntity)) return false;
        WorkerLogDockerEntity that = (WorkerLogDockerEntity) o;
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
        return "WorkerLogDockerEntity{" +
                "workerId=" + workerId +
                ", log='" + log + '\'' +
                ", hasFinishedProducingLog=" + hasFinishedProducingLog +
                '}';
    }
}
