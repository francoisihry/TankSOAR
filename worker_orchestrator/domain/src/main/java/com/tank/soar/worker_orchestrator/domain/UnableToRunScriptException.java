package com.tank.soar.worker_orchestrator.domain;

public class UnableToRunScriptException extends Exception {

    private final WorkerId unknownWorkerId;

    public UnableToRunScriptException(final WorkerId unknownWorkerId) {
        this.unknownWorkerId = unknownWorkerId;
    }

    public WorkerId unknownWorkerId() {
        return unknownWorkerId;
    }

}