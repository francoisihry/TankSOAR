package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public final class RunScriptUseCase implements UseCase<RunScriptCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;
    private final WorkerIdProvider workerIdProvider;

    public RunScriptUseCase(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository,
                            final TransactionalUseCase transactionalUseCase,
                            final WorkerIdProvider workerIdProvider) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
        this.workerIdProvider = Objects.requireNonNull(workerIdProvider);
    }

    @Override
    public Worker execute(final RunScriptCommand command) throws UnableToRunScriptUseCaseException {
        try {
            final WorkerId workerId = this.workerIdProvider.provideNewWorkerId();
            this.transactionalUseCase.begin();
            this.workerRepository.createWorker(workerId, command.script(),
                    LocalDateTime.now(ZoneOffset.UTC), // should use a provider - ie an interface and implementation : better testing
                    LocalDateTime.now(ZoneOffset.UTC)); // should use a provider - ie an interface and implementation : better testing
            this.transactionalUseCase.commit();
            return this.workerContainerManager.runScript(workerId, command.script());
        } catch (final UnableToRunScriptException e) {
            // FIXME
            throw new UnableToRunScriptUseCaseException(e);
        }
    }

}
