package com.tank.soar.worker_orchestrator.domain.usecase;

public interface UseCase<C extends UseCaseCommand, O> {

    O execute(C command) throws UseCaseException;

}
