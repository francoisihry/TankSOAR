package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.List;
import java.util.Objects;

public final class RetrieveWorkerLogsUseCase implements UseCase<RetrieveWorkerLogsCommand, List<? extends LogStream>> {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;

    public RetrieveWorkerLogsUseCase(final WorkerContainerManager workerContainerManager,
                                     final WorkerRepository workerRepository) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
    }

    @Override
    public List<? extends LogStream> execute(final RetrieveWorkerLogsCommand command) throws UseCaseException {
        final WorkerId workerId = Objects.requireNonNull(command.workerId());
        final Boolean stdOut = Objects.requireNonNull(command.stdOut());
        final Boolean stdErr = Objects.requireNonNull(command.stdErr());
        try {
            return workerContainerManager.findLog(workerId, stdOut, stdErr)
                    .orElseGet(() -> {
                        // In this case it has been deleted because the container state is finished.
                        try {
                            return workerRepository.getLog(workerId, stdOut, stdErr);
                        } catch (final UnknownWorkerException unknownWorkerException) {
                            throw new UnknownWorkerUseCaseException(unknownWorkerException.unknownWorkerId());
                        }
                    });
        } catch (final UnknownWorkerException unknownWorkerException) {
            throw new UnknownWorkerUseCaseException(unknownWorkerException.unknownWorkerId());
        }
    }

}
