package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import io.quarkus.runtime.Startup;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped
@Startup
public class DockerWorkerContainerManager implements WorkerContainerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWorkerContainerManager.class);

    public static final String WORKER_ID = "workerId";
    public static final String IMAGE_TYPE = "TankSOAR";
    public static final String PYTHON_IMAGE_TYPE = "Python";

    private final DockerClient dockerClient;

    private final String pythonImageId;
    private final WorkerLockMechanism workerLockMechanism;

    // max 10 // running containers TODO I should change it to be dynamic
    private final ExecutorService dockerContainersLifecycle = Executors.newFixedThreadPool(10);

    private final Event<DockerStateChanged> dockerStateChangedEvent;
    private final Event<WorkerStateChanged> workerStateChangedEvent;
    private final DockerLastUpdateStateDateProvider dockerLastUpdateStateDateProvider;

    public DockerWorkerContainerManager(final DockerClient dockerClient,
                                        @Any final WorkerLockMechanism workerLockMechanism,
                                        final Event<DockerStateChanged> dockerStateChangedEvent,
                                        final Event<WorkerStateChanged> workerStateChangedEvent,
                                        final DockerLastUpdateStateDateProvider dockerLastUpdateStateDateProvider) throws URISyntaxException {
        this.dockerClient = Objects.requireNonNull(dockerClient);
        this.workerLockMechanism = Objects.requireNonNull(workerLockMechanism);
        this.dockerStateChangedEvent = Objects.requireNonNull(dockerStateChangedEvent);
        this.workerStateChangedEvent = Objects.requireNonNull(workerStateChangedEvent);
        this.dockerLastUpdateStateDateProvider = Objects.requireNonNull(dockerLastUpdateStateDateProvider);
        // build the python image if not present
        final Optional<Image> pythonTankSOARDockerImage = dockerClient.listImagesCmd()
                .withLabelFilter(Collections.singletonMap(IMAGE_TYPE, PYTHON_IMAGE_TYPE))
                .exec()
                .stream()
                .findAny();
        if (pythonTankSOARDockerImage.isPresent()) {
            LOGGER.info("Image python is present, not needed to build it");
            pythonImageId = pythonTankSOARDockerImage.get().getId();
        } else {
            LOGGER.info("Image python is not present, need to build it");
            final BuildImageResultCallback buildImageResultCallback = new BuildImageResultCallback();
            final File pythonDockerfile = new File(getClass().getResource("/PythonDockerfile").toURI());
            dockerClient.buildImageCmd(pythonDockerfile)
                    .withTags(Collections.singleton("tanksoar_worker_python:latest"))
                    .withLabels(Collections.singletonMap(IMAGE_TYPE, PYTHON_IMAGE_TYPE))
                    .exec(buildImageResultCallback);
            pythonImageId = buildImageResultCallback.awaitImageId();
            LOGGER.info("End building image python");
        }
        LOGGER.info("End image initialization");
    }

    @Override
    public Worker runScript(final WorkerId workerId, final String script) throws UnableToRunScriptException {
        workerLockMechanism.lock(workerId);
        final InspectContainerResponse containerCreated;
        try {
            // create container
            final CreateContainerResponse workerContainer = dockerClient.createContainerCmd(pythonImageId)
                    .withName(workerId.id())
                    .withLabels(Collections.singletonMap(WORKER_ID, workerId.id()))
                    .exec();
            LOGGER.info(String.format("Container workerId '%s' created.", workerId.id()));

            // copy script file into container
            final Path tempFile = Files.createTempFile(null, null);
            try (final BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile()))) {
                bw.write(script);
            }

            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 final TarArchiveOutputStream tar = new TarArchiveOutputStream(bos)) {
                final TarArchiveEntry entry = new TarArchiveEntry("script.py");
                entry.setSize(script.getBytes().length);
                entry.setMode(0700);
                tar.putArchiveEntry(entry);
                tar.write(script.getBytes());
                tar.closeArchiveEntry();
                tar.close();
                try (final InputStream is = new ByteArrayInputStream(bos.toByteArray())) {
                    dockerClient.copyArchiveToContainerCmd(workerContainer.getId())
                            .withTarInputStream(is)
                            .withRemotePath("/tmp")
                            .exec();
                }
            }
            LOGGER.info(String.format("Script copied into container workerId '%s'", workerId.id()));
            Files.deleteIfExists(tempFile);
            containerCreated = dockerClient.inspectContainerCmd(workerId.id()).exec();
        } catch (final IOException ioException) {
            throw new UnableToRunScriptException(workerId, ioException);
        } finally {
            workerLockMechanism.unlock(workerId);
        }
        final UTCZonedDateTime dockerStateChangedDate = dockerLastUpdateStateDateProvider.lastUpdateStateDate(containerCreated);
        dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                .withWorkerId(workerId)
                .withContainer(containerCreated)
                .withDockerStateChangedDate(dockerStateChangedDate)
                .withStdResponses(Collections.emptyList())
                .build());
        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                .withWorkerId(workerId)
                .withWorkerStatus(DockerContainerStatus
                        .fromDockerStatus(containerCreated.getState().getStatus())
                        .toWorkerStatus())
                .withLastUpdateStateDate(dockerStateChangedDate)
                .build();
        workerStateChangedEvent.fire(new WorkerStateChanged(workerDockerContainer));
        dockerContainersLifecycle.submit(new DockerLifecycleRunnable(workerId, dockerClient,
                dockerStateChangedEvent,
                workerStateChangedEvent,
                dockerLastUpdateStateDateProvider,
                workerLockMechanism));
        return workerDockerContainer;
    }

    @Override
    public List<? extends Worker> listAllContainers() {
        // TODO should use a global lock
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> container.getLabels() != null)
                .filter(container -> container.getLabels().containsKey(WORKER_ID))
                .map(container -> {
                    final InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
                    return WorkerDockerContainer.newBuilder()
                            .withWorkerId(new WorkerId(container.getLabels().get(WORKER_ID)))
                            .withWorkerStatus(DockerContainerStatus.fromDockerStatus(container.getState()).toWorkerStatus())
                            .withLastUpdateStateDate(dockerLastUpdateStateDateProvider.lastUpdateStateDate(inspectContainerResponse))
                            .build();})
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Worker> findContainer(final WorkerId workerId) {
        return inspectWorkerContainer(workerId)
                .map(container -> WorkerDockerContainer.newBuilder()
                        .withWorkerId(workerId)
                        .withWorkerStatus(DockerContainerStatus.fromDockerStatus(container.getState().getStatus()).toWorkerStatus())
                        .withLastUpdateStateDate(dockerLastUpdateStateDateProvider.lastUpdateStateDate(container))
                        .build());
    }

    @Override
    public void deleteContainer(final WorkerId workerId) throws UnknownWorkerException {
        inspectWorkerContainer(workerId)
                .map(inspectContainerResponse -> {
                    workerLockMechanism.lock(workerId);
                    try {
                        dockerClient.removeContainerCmd(inspectContainerResponse.getId())
                                .withForce(true)
                                .exec();
                    } finally {
                        workerLockMechanism.unlock(workerId);
                    }
                    return inspectContainerResponse;
                })
                .orElseThrow(() -> new UnknownWorkerException(workerId));
    }

    @Override
    public Optional<List<? extends LogStream>> findLog(final WorkerId workerId,
                                                       final Boolean stdOut,
                                                       final Boolean stdErr) {
        return inspectWorkerContainer(workerId)
                .map(inspectContainerResponse -> {
                    workerLockMechanism.lock(workerId);
                    try {
                        final LoggingResultCallbackAdapter loggingResultCallbackAdapter = new LoggingResultCallbackAdapter(workerId);
                        dockerClient
                                .logContainerCmd(inspectContainerResponse.getId())
                                .withStdOut(stdOut)
                                .withStdErr(stdErr)
                                .withFollowStream(false)
                                .withTailAll()
                                .exec(loggingResultCallbackAdapter);
                        loggingResultCallbackAdapter.awaitCompletion();
                        return loggingResultCallbackAdapter.getStdResponses();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        workerLockMechanism.unlock(workerId);
                    }
                });
    }

    private Optional<InspectContainerResponse> inspectWorkerContainer(final WorkerId workerId) {
        try {
            return Optional.of(dockerClient.inspectContainerCmd(workerId.id()).exec());
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

}
