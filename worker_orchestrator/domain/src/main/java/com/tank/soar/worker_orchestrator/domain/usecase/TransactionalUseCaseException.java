package com.tank.soar.worker_orchestrator.domain.usecase;

public class TransactionalUseCaseException extends RuntimeException {

    public TransactionalUseCaseException(final Throwable cause) {
        super(cause);
    }
}
