package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.usecase.TransactionalUseCaseException;
import com.tank.soar.worker_orchestrator.domain.usecase.TransactionalUseCase;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.UserTransaction;
import java.util.Objects;

@ApplicationScoped
public class UserTransactionTransactionalUseCase implements TransactionalUseCase {

    private final UserTransaction userTransaction;

    public UserTransactionTransactionalUseCase(final UserTransaction userTransaction) {
        this.userTransaction = Objects.requireNonNull(userTransaction);
    }

    @Override
    public void begin() throws TransactionalUseCaseException {
        try {
            userTransaction.begin();
        } catch (final Exception exception) {
            throw new TransactionalUseCaseException(exception);
        }
    }

    @Override
    public void commit() throws TransactionalUseCaseException {
        try {
            userTransaction.commit();
        } catch (final Exception exception) {
            throw new TransactionalUseCaseException(exception);
        }
    }

    @Override
    public void rollback() throws TransactionalUseCaseException {
        try {
            userTransaction.rollback();
        } catch (final Exception exception) {
            throw new TransactionalUseCaseException(exception);
        }
    }
}
