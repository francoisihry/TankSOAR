package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageWorkersContainersLifeCycleUseCase implements UseCase<VoidCommand, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageWorkersContainersLifeCycleUseCase.class);

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;

    public ManageWorkersContainersLifeCycleUseCase(final WorkerContainerManager workerContainerManager,
                                                   final WorkerRepository workerRepository) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
    }

    @Override
    public Void execute(final VoidCommand command) {
        final List<? extends Worker> workerContainers = workerContainerManager.listAllContainers();
        workerContainers.stream()
                .filter(workerContainer -> {
                    final boolean hasWorker = workerRepository.hasWorker(workerContainer.workerId());
                    if (!hasWorker) {
                        LOGGER.warn(String.format("A worker container is present but not the one in database '%s'. The worker container will not be removed.", workerContainer.workerId().id()));
                        return false;
                    }
                    return true;
                })
                .peek(worker -> {
                    try {
                        final ContainerInformation containerMetadata = workerContainerManager.getContainerMetadata(worker.workerId());
                        final List<? extends LogStream> logStreams = workerContainerManager.findLog(worker.workerId(), Boolean.TRUE, Boolean.TRUE)
                                .orElseThrow(() -> new UnknownWorkerException(worker.workerId()));
                        workerRepository.saveWorker(worker, containerMetadata, logStreams);
                        LOGGER.info(String.format("Container state workerId '%s' saved", worker.workerId().id()));
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        LOGGER.warn(String.format("Unable to get container state '%s'", unknownWorkerException.unknownWorkerId()));
                    } catch (final RuntimeException e) {
                        throw e;
                    }
                })
                .filter(Worker::hasFinished)
                .forEach(finishedWorker -> {
                    try {
                        workerContainerManager.deleteContainer(finishedWorker.workerId());
                        LOGGER.info(String.format("Container workerId '%s' removed", finishedWorker.workerId().id()));
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        LOGGER.warn(String.format("Unable to remove unknown container '%s'", unknownWorkerException.unknownWorkerId()));
                    }
                });
        return null;
    }

}
