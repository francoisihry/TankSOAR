package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.Worker;

import java.util.Objects;

public final class NewWorkerEvent {

    private final Worker worker;

    public NewWorkerEvent(final Worker worker) {
        this.worker = Objects.requireNonNull(worker);
    }

    public Worker worker() {
        return worker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewWorkerEvent)) return false;
        NewWorkerEvent that = (NewWorkerEvent) o;
        return Objects.equals(worker, that.worker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worker);
    }

    @Override
    public String toString() {
        return "WorkerStateChanged{" +
                "worker=" + worker +
                '}';
    }
}
