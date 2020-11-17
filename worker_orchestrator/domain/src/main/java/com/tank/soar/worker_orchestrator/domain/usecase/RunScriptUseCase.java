package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public final class RunScriptUseCase implements UseCase<RunScriptCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final WorkerIdProvider workerIdProvider;

    public RunScriptUseCase(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository,
                            final WorkerIdProvider workerIdProvider) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.workerIdProvider = Objects.requireNonNull(workerIdProvider);
    }

    @Override
    public Worker execute(final RunScriptCommand command) throws UnableToRunScriptUseCaseException {
        try {
            final WorkerId workerId = this.workerIdProvider.provideNewWorkerId();
            final UTCZonedDateTime executionDate = UTCZonedDateTime.now();// should use a provider - ie an interface and implementation : better testing
            this.workerRepository.createWorker(workerId, command.script(),
                    executionDate,
                    executionDate);
            return this.workerContainerManager.runScript(workerId, command.script());
        } catch (final UnableToRunScriptException e) {
            // FIXME
            throw new UnableToRunScriptUseCaseException(e);
        }
    }

}
