package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;

public interface WorkerEvent {

    WorkerId workerId();

    EventType eventType();

    UTCZonedDateTime eventDate();

    UserEventType userEventType();

    DockerContainerStatus dockerContainerStatus();

    default WorkerStatus workerStatus() {
        switch (eventType()) {
            case USER:
                switch (userEventType()) {
                    case CREATION_REQUESTED:
                        return WorkerStatus.CREATION_REQUESTED;
                    case WORKER_MANUALLY_STOPPED:
                        return WorkerStatus.STOP_REQUESTED;
                    default:
                        throw new IllegalStateException();
                }
            case DOCKER:
                return dockerContainerStatus().toWorkerStatus();
            default:
                throw new IllegalStateException("should not be here");
        }
    }



}
