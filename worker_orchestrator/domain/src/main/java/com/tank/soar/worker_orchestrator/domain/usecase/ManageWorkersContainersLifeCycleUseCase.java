package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageWorkersContainersLifeCycleUseCase<INFRA extends ContainerMetadata> implements UseCase<VoidCommand, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageWorkersContainersLifeCycleUseCase.class);

    private final WorkerContainerManager<INFRA> workerContainerManager;
    private final WorkerRepository<INFRA> workerRepository;
    private final TransactionalUseCase transactionalUseCase;

    public ManageWorkersContainersLifeCycleUseCase(final WorkerContainerManager<INFRA> workerContainerManager,
                                                   final WorkerRepository<INFRA> workerRepository,
                                                   final TransactionalUseCase transactionalUseCase) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
    }

    @Override
    public Void execute(final VoidCommand command) throws UseCaseException {
        final List<Worker> workerContainers = workerContainerManager.listAllContainers();
        workerContainers.stream()
                .peek(worker -> {
                    try {
                        final INFRA containerMetadata = workerContainerManager.getContainerMetadata(worker.workerId());
                        transactionalUseCase.begin();
                        workerRepository.saveWorker(worker, containerMetadata);
                        transactionalUseCase.commit();
                        LOGGER.info(String.format("Container state workerId '%s' saved", worker.workerId().id()));
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        LOGGER.warn(String.format("Unable to get container state '%s'", unknownWorkerException.unknownWorkerId()));
                    }
                })
                .filter(Worker::hasFinished)
                .forEach(finishedWorker -> {
                    try {
                        workerContainerManager.removeContainer(finishedWorker.workerId());
                        LOGGER.info(String.format("Container workerId '%s' removed", finishedWorker.workerId().id()));
                    } catch (final UnknownWorkerException unknownWorkerException) {
                        LOGGER.warn(String.format("Unable to remove unknown container '%s'", unknownWorkerException.unknownWorkerId()));
                    }
                });
        return null;
    }

}
