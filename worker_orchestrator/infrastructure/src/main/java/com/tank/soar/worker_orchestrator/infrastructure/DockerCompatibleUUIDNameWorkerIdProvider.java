package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerIdProvider;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class DockerCompatibleUUIDNameWorkerIdProvider implements WorkerIdProvider {

    @Override
    public WorkerId provideNewWorkerId() {
        return new WorkerId(UUID.randomUUID().toString());
    }

}
