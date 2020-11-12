package com.tank.soar.worker_orchestrator.domain;

public class UnknownWorkerException extends Exception {

    private final WorkerId unknownWorkerId;

    public UnknownWorkerException(final WorkerId unknownWorkerId) {
        this.unknownWorkerId = unknownWorkerId;
    }

    public WorkerId unknownWorkerId() {
        return unknownWorkerId;
    }

}
