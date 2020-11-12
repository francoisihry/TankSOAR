package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

public class UnknownWorkerUseCaseException extends UseCaseException {

    private final WorkerId unknownWorkerId;

    public UnknownWorkerUseCaseException(final WorkerId unknownWorkerId) {
        this.unknownWorkerId = unknownWorkerId;
    }

    public WorkerId unknownWorkerId() {
        return unknownWorkerId;
    }

}
