package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public class RetrieveWorkerStdOutUseCase implements UseCase<RetrieveWorkerStdOutCommand, String> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public RetrieveWorkerStdOutUseCase(final WorkerContainerManager workerContainerManager,
                                       final WorkerRepository workerRepository,
                                       final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public String execute(final RetrieveWorkerStdOutCommand command) throws UseCaseException {
        final WorkerId workerId = command.workerId();
        try {
            return workerContainerManager.getStdOut(workerId);
        } catch (final UnknownWorkerException unknownWorkerException) {
            // can be expected
            try {
                transactionalUseCase.begin();
                final String stdOut = workerRepository.getStdOut(workerId);
                transactionalUseCase.commit();
                return stdOut;
            } catch (final UnknownWorkerException unknownWorkerException1) {
                transactionalUseCase.rollback();
                throw new UnknownWorkerUseCaseException(unknownWorkerException1.unknownWorkerId());
            }
        }
    }

}
