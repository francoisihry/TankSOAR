package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public class RetrieveWorkerStdErrUseCase implements UseCase<RetrieveWorkerStdErrCommand, WorkerLog> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public RetrieveWorkerStdErrUseCase(final WorkerContainerManager workerContainerManager,
                                       final WorkerRepository workerRepository,
                                       final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public WorkerLog execute(final RetrieveWorkerStdErrCommand command) throws UseCaseException {
        final WorkerId workerId = command.workerId();
        return workerContainerManager.getStdErr(workerId)
                .orElseGet(() -> {
                    // In this case it has been deleted because the container state is finished.
                    try {
                        transactionalUseCase.begin();
                        final WorkerLog stdErr = workerRepository.getStdErr(workerId);
                        transactionalUseCase.commit();
                        return stdErr;
                    } catch (final UnknownWorkerException unknownWorkerException1) {
                        transactionalUseCase.rollback();
                        throw new UnknownWorkerUseCaseException(unknownWorkerException1.unknownWorkerId());
                    }
                });
    }

}