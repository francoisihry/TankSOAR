package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerLog;

import java.util.Objects;

public final class WorkerLogDockerContainer implements WorkerLog {

    private final WorkerId workerId;
    private final String log;
    private final Boolean hasFinishedProducingLog;

    public WorkerLogDockerContainer(final WorkerId workerId, final String log, final Boolean hasFinishedProducingLog) {
        this.workerId = Objects.requireNonNull(workerId);
        this.log = Objects.requireNonNull(log);
        this.hasFinishedProducingLog = Objects.requireNonNull(hasFinishedProducingLog);
    }

    public WorkerLogDockerContainer(final WorkerDockerContainer workerDockerContainer, final String log) {
        this(workerDockerContainer.workerId(), log, workerDockerContainer.hasFinished());
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
        if (!(o instanceof WorkerLogDockerContainer)) return false;
        WorkerLogDockerContainer that = (WorkerLogDockerContainer) o;
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
        return "WorkerLogDockerContainer{" +
                "workerId=" + workerId +
                ", log='" + log + '\'' +
                ", hasFinishedProducingLog=" + hasFinishedProducingLog +
                '}';
    }
}
