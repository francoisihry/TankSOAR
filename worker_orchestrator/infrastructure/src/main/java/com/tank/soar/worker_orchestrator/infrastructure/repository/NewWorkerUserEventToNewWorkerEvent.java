package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.infrastructure.NewWorkerEvent;
import com.tank.soar.worker_orchestrator.infrastructure.container.WorkerDockerContainer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import java.util.Objects;

@ApplicationScoped
public class NewWorkerUserEventToNewWorkerEvent {

    final Event<NewWorkerEvent> workerStateChangedEvent;

    public NewWorkerUserEventToNewWorkerEvent(final Event<NewWorkerEvent> workerStateChangedEvent) {
        this.workerStateChangedEvent = Objects.requireNonNull(workerStateChangedEvent);
    }

    void onDockerStateChanged(@Observes final NewWorkerEntityEvent newWorkerEntityEvent) {
        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                .withWorkerId(newWorkerEntityEvent.workerId())
                .withWorkerStatus(newWorkerEntityEvent.workerStatus())
                .withLastUpdateStateDate(newWorkerEntityEvent.eventDate())
                .build();
        workerStateChangedEvent.fire(new NewWorkerEvent(workerDockerContainer));
    }

}
