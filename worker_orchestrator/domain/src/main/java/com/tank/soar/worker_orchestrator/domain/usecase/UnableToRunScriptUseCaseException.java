package com.tank.soar.worker_orchestrator.domain.usecase;

public class UnableToRunScriptUseCaseException extends UseCaseException {

    public UnableToRunScriptUseCaseException(final Throwable cause) {
        super(cause);
    }
}
