package com.tank.soar.worker_orchestrator.infrastructure.service;

import com.tank.soar.worker_orchestrator.domain.usecase.ManageWorkersContainersLifeCycleUseCase;
import com.tank.soar.worker_orchestrator.domain.usecase.VoidCommand;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ManageWorkersContainersLifeCycleUseCaseExecutor {

    private final ManageWorkersContainersLifeCycleUseCase manageWorkersContainersLifeCycleUseCase;

    public ManageWorkersContainersLifeCycleUseCaseExecutor(final ManageWorkersContainersLifeCycleUseCase manageWorkersContainersLifeCycleUseCase) {
        this.manageWorkersContainersLifeCycleUseCase = Objects.requireNonNull(manageWorkersContainersLifeCycleUseCase);
    }

    // faire un test qui compte le nombre d'executions (2 par exemple) en X seconds ...
    @Scheduled(every="10s")
    public void scheduleExecution() {
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());
    }

}
