package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.Objects;

public class StopRunningScriptUseCase implements UseCase<StopRunningScriptCommand, Worker> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    public StopRunningScriptUseCase(final WorkerContainerManager workerContainerManager,
                                    final WorkerRepository workerRepository,
                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.utcZonedDateTimeProvider = Objects.requireNonNull(utcZonedDateTimeProvider);
    }

    @Override
    public Worker execute(final StopRunningScriptCommand command) throws UseCaseException {
        final WorkerId workerId = command.workerId();
        return workerContainerManager.findWorker(workerId)
                .map(worker -> {
                    try {
                        workerContainerManager.deleteWorker(workerId);
                        return workerRepository.markWorkerAsManuallyStopped(workerId, utcZonedDateTimeProvider.now());
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        throw new UnknownWorkerUseCaseException(unknownWorkerException.unknownWorkerId());
                    }
                })
                .orElseThrow(() -> new WorkerAlreadyDeletedUseCaseException(workerId));
    }

}
