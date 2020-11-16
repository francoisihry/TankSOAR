package com.tank.soar.worker_orchestrator.domain;

public class UnableToRunScriptException extends Exception {

    private final WorkerId workerId;

    public UnableToRunScriptException(final WorkerId workerId, final Throwable cause) {
        super(cause);
        this.workerId = workerId;
    }

    public WorkerId workerId() {
        return workerId;
    }

}