package com.tank.soar.worker_orchestrator.domain.usecase;

public class UseCaseException extends Exception {

    public UseCaseException(final Throwable cause) {
        super(cause);
    }

    public UseCaseException() {
        super();
    }
}
