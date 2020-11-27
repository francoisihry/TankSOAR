package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public class GetWorkerUseCase implements UseCase<GetWorkerCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;

    public GetWorkerUseCase(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
    }

    @Override
    public Worker execute(final GetWorkerCommand command) throws UseCaseException {
        final WorkerId workerId = command.workerId();
        return workerContainerManager.findWorker(workerId)
                .orElseGet(() -> {
                    // In this case it has been deleted because the container state is finished.
                    try {
                        return workerRepository.getWorker(workerId);
                    } catch (final UnknownWorkerException unknownWorkerException1) {
                        throw new UnknownWorkerUseCaseException(unknownWorkerException1.unknownWorkerId());
                    }
                });
    }

}
