package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.util.Collections;
import java.util.Objects;

public final class DockerLifecycleRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerLifecycleRunnable.class);

    private final WorkerId workerId;
    private final DockerClient dockerClient;
    private final Event<DockerStateChanged> dockerStateChangedEvent;
    private final Event<WorkerStateChanged> workerStateChangedEvent;
    private final DockerLastUpdateStateDateProvider dockerLastUpdateStateDateProvider;
    private final WorkerLockMechanism workerLockMechanism;

    public DockerLifecycleRunnable(final WorkerId workerId,
                                   final DockerClient dockerClient,
                                   final Event<DockerStateChanged> dockerStateChangedEvent,
                                   final Event<WorkerStateChanged> workerStateChangedEvent,
                                   final DockerLastUpdateStateDateProvider dockerLastUpdateStateDateProvider,
                                   final WorkerLockMechanism workerLockMechanism) {
        this.workerId = Objects.requireNonNull(workerId);
        this.dockerClient = Objects.requireNonNull(dockerClient);
        this.dockerStateChangedEvent = Objects.requireNonNull(dockerStateChangedEvent);
        this.workerStateChangedEvent = Objects.requireNonNull(workerStateChangedEvent);
        this.dockerLastUpdateStateDateProvider = Objects.requireNonNull(dockerLastUpdateStateDateProvider);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
    }

    @Override
    public void run() {
        try {
            startContainer();
            waitForContainerEndOfLife();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startContainer() {
        workerLockMechanism.lock(workerId);
        final InspectContainerResponse container;
        try {
            dockerClient.startContainerCmd(workerId.id()).exec();
            container = dockerClient.inspectContainerCmd(workerId.id()).exec();
        } finally {
            workerLockMechanism.unlock(workerId);
        }
        final UTCZonedDateTime dockerStateChangedDate = dockerLastUpdateStateDateProvider.lastUpdateStateDate(container);
        dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                .withWorkerId(workerId)
                .withContainer(container)
                .withDockerStateChangedDate(dockerStateChangedDate)
                .withStdResponses(Collections.emptyList())
                .build());
        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                .withWorkerId(workerId)
                .withWorkerStatus(DockerContainerStatus
                        .fromDockerStatus(container.getState().getStatus())
                        .toWorkerStatus())
                .withLastUpdateStateDate(dockerStateChangedDate)
                .build();
        workerStateChangedEvent.fire(new WorkerStateChanged(workerDockerContainer));
    }

    private void waitForContainerEndOfLife() throws InterruptedException {
        final WaitContainerResultCallback waitContainerResultCallback = dockerClient.waitContainerCmd(workerId.id()).start();
        waitContainerResultCallback.awaitCompletion();
        workerLockMechanism.lock(workerId);
        final InspectContainerResponse container;
        final LoggingResultCallbackAdapter loggingResultCallbackAdapter;
        try {
            container = dockerClient.inspectContainerCmd(workerId.id()).exec();
            loggingResultCallbackAdapter = new LoggingResultCallbackAdapter(workerId);
            dockerClient
                    .logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .withTailAll()
                    .exec(loggingResultCallbackAdapter);
            loggingResultCallbackAdapter.awaitCompletion();
            final UTCZonedDateTime dockerStateChangedDate = dockerLastUpdateStateDateProvider.lastUpdateStateDate(container);
            dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                    .withWorkerId(workerId)
                    .withContainer(container)
                    .withDockerStateChangedDate(dockerStateChangedDate)
                    .withStdResponses(loggingResultCallbackAdapter.getStdResponses())
                    .build());
            final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                    .withWorkerId(workerId)
                    .withWorkerStatus(DockerContainerStatus
                            .fromDockerStatus(container.getState().getStatus())
                            .toWorkerStatus())
                    .withLastUpdateStateDate(dockerStateChangedDate)
                    .build();
            workerStateChangedEvent.fire(new WorkerStateChanged(workerDockerContainer));
        } catch (final NotFoundException notFoundException) {
            // in this case the container has been removed... Expected as the user should be able to remove it
        } catch (final ConflictException conflictException) {
            // 409: can not get logs from container which is dead or marked for removal
        } finally {
            workerLockMechanism.unlock(workerId);
        }
    }

}
