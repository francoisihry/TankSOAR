package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

public final class RunScriptUseCase implements UseCase<RunScriptCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public RunScriptUseCase(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository,
                            final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public Worker execute(final RunScriptCommand command) throws UnableToRunScriptUseCaseException {
        try {
            final Worker worker = this.workerContainerManager.runScript(command.script());
            final ContainerInformation containerMetadata = workerContainerManager.getContainerMetadata(worker.workerId());
            final WorkerLog stdOut = workerContainerManager.getStdOut(worker.workerId()).get();
            final WorkerLog stdErr = workerContainerManager.getStdErr(worker.workerId()).get();
            Validate.validState(stdOut.workerId().equals(worker.workerId()));
            Validate.validState(stdOut.hasFinishedProducingLog().equals(worker.hasFinished()));
            Validate.validState(stdErr.workerId().equals(worker.workerId()));
            Validate.validState(stdErr.hasFinishedProducingLog().equals(worker.hasFinished()));
            this.transactionalUseCase.begin();
            this.workerRepository.createWorker(worker, command.script(), containerMetadata, stdOut, stdErr);
            this.transactionalUseCase.commit();
            return worker;
        } catch (final UnableToRunScriptException | UnknownWorkerException e) {
            throw new UnableToRunScriptUseCaseException(e);
        }
    }

}
