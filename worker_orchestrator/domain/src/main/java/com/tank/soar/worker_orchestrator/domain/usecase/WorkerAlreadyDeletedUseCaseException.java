package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

public class WorkerAlreadyDeletedUseCaseException extends UseCaseException {

    private final WorkerId workerId;

    public WorkerAlreadyDeletedUseCaseException(final WorkerId workerId) {
        this.workerId = workerId;
    }

    public WorkerId workerId() {
        return workerId;
    }

}
