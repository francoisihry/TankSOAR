package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.WorkerContainerManager;
import com.tank.soar.worker_orchestrator.domain.WorkerIdProvider;
import com.tank.soar.worker_orchestrator.domain.WorkerRepository;
import com.tank.soar.worker_orchestrator.domain.usecase.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Objects;

public class UseCaseProducers {

    private final WorkerContainerManager workerContainerManager;
    private final WorkerRepository workerRepository;
    private final TransactionalUseCase transactionalUseCase;
    private final WorkerIdProvider workerIdProvider;

    public UseCaseProducers(final WorkerContainerManager workerContainerManager,
                            final WorkerRepository workerRepository,
                            final TransactionalUseCase transactionalUseCase,
                            final WorkerIdProvider workerIdProvider) {
        this.workerContainerManager = Objects.requireNonNull(workerContainerManager);
        this.workerRepository = Objects.requireNonNull(workerRepository);
        this.transactionalUseCase = Objects.requireNonNull(transactionalUseCase);
        this.workerIdProvider = Objects.requireNonNull(workerIdProvider);
    }

    @Produces
    @ApplicationScoped
    public GetWorkerUseCase produceGetWorkerUseCase() {
        return new GetWorkerUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Produces
    @ApplicationScoped
    public ListWorkersUseCase produceListWorkersUseCase() {
        return new ListWorkersUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Produces
    @ApplicationScoped
    public RetrieveWorkerStdErrUseCase produceRetrieveWorkerStdErrUseCase() {
        return new RetrieveWorkerStdErrUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Produces
    @ApplicationScoped
    public RetrieveWorkerStdOutUseCase produceRetrieveWorkerStdOutUseCase() {
        return new RetrieveWorkerStdOutUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Produces
    @ApplicationScoped
    public RunScriptUseCase produceRunScriptUseCase() {
        return new RunScriptUseCase(workerContainerManager, workerRepository, transactionalUseCase, workerIdProvider);
    }

    @Produces
    @ApplicationScoped
    public ManageWorkersContainersLifeCycleUseCase produceManageWorkersContainersLifeCycleUseCase() {
        return new ManageWorkersContainersLifeCycleUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

}
