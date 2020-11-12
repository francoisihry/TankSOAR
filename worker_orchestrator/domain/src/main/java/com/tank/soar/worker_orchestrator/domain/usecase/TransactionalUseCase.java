package com.tank.soar.worker_orchestrator.domain.usecase;

public interface TransactionalUseCase {

    void begin() throws TransactionalUseCaseException;

    void commit() throws TransactionalUseCaseException;

    void rollback() throws TransactionalUseCaseException;

}
