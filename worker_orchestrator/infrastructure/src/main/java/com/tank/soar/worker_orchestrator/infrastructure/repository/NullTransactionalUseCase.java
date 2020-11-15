package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.usecase.TransactionalUseCaseException;
import com.tank.soar.worker_orchestrator.domain.usecase.TransactionalUseCase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NullTransactionalUseCase implements TransactionalUseCase {

    @Override
    public void begin() throws TransactionalUseCaseException {
    }

    @Override
    public void commit() throws TransactionalUseCaseException {
    }

    @Override
    public void rollback() throws TransactionalUseCaseException {
    }
}
