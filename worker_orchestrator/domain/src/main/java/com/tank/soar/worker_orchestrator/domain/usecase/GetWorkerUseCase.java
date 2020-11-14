package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public class GetWorkerUseCase implements UseCase<GetWorkerCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public GetWorkerUseCase(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository,
                            final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public Worker execute(final GetWorkerCommand command) throws UseCaseException {
        final WorkerId workerId = command.workerId();
        return workerContainerManager.findContainer(workerId)
                .orElseGet(() -> {
                    // In this case it has been deleted because the container state is finished.
                    try {
                        transactionalUseCase.begin();
                        final Worker worker = workerRepository.getWorker(workerId);
                        transactionalUseCase.commit();
                        return worker;
                    } catch (final UnknownWorkerException unknownWorkerException1) {
                        transactionalUseCase.rollback();
                        throw new UnknownWorkerUseCaseException(unknownWorkerException1.unknownWorkerId());
                    }
                });
    }

}
