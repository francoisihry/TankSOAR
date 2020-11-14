package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageWorkersContainersLifeCycleUseCase implements UseCase<VoidCommand, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageWorkersContainersLifeCycleUseCase.class);

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public ManageWorkersContainersLifeCycleUseCase(final WorkerContainerManager workerContainerManager,
                                                   final WorkerRepository workerRepository,
                                                   final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public Void execute(final VoidCommand command) {
        final List<? extends Worker> workerContainers = workerContainerManager.listAllContainers();
        workerContainers.stream()
                .peek(worker -> {
                    try {
                        final ContainerInformation containerMetadata = workerContainerManager.getContainerMetadata(worker.workerId());
                        final WorkerLog stdOut = workerContainerManager.getStdOut(worker.workerId()).get();
                        final WorkerLog stdErr = workerContainerManager.getStdErr(worker.workerId()).get();
                        Validate.validState(stdOut.workerId().equals(worker.workerId()));
                        Validate.validState(stdOut.hasFinishedProducingLog().equals(worker.hasFinished()));
                        Validate.validState(stdErr.workerId().equals(worker.workerId()));
                        Validate.validState(stdErr.hasFinishedProducingLog().equals(worker.hasFinished()));
                        transactionalUseCase.begin();
                        workerRepository.saveWorker(worker, containerMetadata, stdOut, stdErr);
                        transactionalUseCase.commit();
                        LOGGER.info(String.format("Container state workerId '%s' saved", worker.workerId().id()));
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        LOGGER.warn(String.format("Unable to get container state '%s'", unknownWorkerException.unknownWorkerId()));
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
