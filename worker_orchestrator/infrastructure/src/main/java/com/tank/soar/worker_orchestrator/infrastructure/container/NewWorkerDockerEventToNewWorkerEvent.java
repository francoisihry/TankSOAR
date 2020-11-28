package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.infrastructure.NewWorkerEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import java.util.Objects;

@ApplicationScoped
public class NewWorkerDockerEventToNewWorkerEvent {

    final Event<NewWorkerEvent> workerStateChangedEvent;

    public NewWorkerDockerEventToNewWorkerEvent(final Event<NewWorkerEvent> workerStateChangedEvent) {
        this.workerStateChangedEvent = Objects.requireNonNull(workerStateChangedEvent);
    }

    void onDockerStateChanged(@Observes final NewWorkerDockerEvent newWorkerDockerEvent) {
        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                .withWorkerId(newWorkerDockerEvent.workerId())
                .withWorkerStatus(DockerContainerStatus
                        .fromDockerStatus(newWorkerDockerEvent.container().getState().getStatus())
                        .toWorkerStatus())
                .withLastUpdateStateDate(newWorkerDockerEvent.dockerStateChangedDate())
                .build();
        workerStateChangedEvent.fire(new NewWorkerEvent(workerDockerContainer));
    }

}
