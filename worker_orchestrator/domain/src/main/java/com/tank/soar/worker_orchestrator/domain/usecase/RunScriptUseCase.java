package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public final class RunScriptUseCase<INFRA extends ContainerMetadata> implements UseCase<RunScriptCommand, Worker> {

    private final WorkerContainerManager<INFRA> workerContainerManager;
    private final WorkerRepository<INFRA> workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public RunScriptUseCase(final WorkerContainerManager<INFRA> workerContainerManager,
                            final WorkerRepository<INFRA> workerRepository,
                            final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public Worker execute(final RunScriptCommand command) throws UnableToRunScriptUseCaseException {
        try {
            final Worker worker = this.workerContainerManager.runScript(command.script());
            final INFRA containerMetadata = workerContainerManager.getContainerMetadata(worker.workerId());
            this.transactionalUseCase.begin();
            this.workerRepository.saveWorker(worker, containerMetadata);
            this.transactionalUseCase.commit();
            return worker;
        } catch (final UnableToRunScriptException | UnknownWorkerException e) {
            throw new UnableToRunScriptUseCaseException(e);
        }
    }

}
