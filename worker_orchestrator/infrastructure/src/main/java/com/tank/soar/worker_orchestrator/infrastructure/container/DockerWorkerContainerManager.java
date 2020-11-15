package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.tank.soar.worker_orchestrator.domain.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DockerWorkerContainerManager implements WorkerContainerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWorkerContainerManager.class);

    public static final String WORKER_ID = "workerId";
    public static final String IMAGE_TYPE = "TankSOAR";
    public static final String PYTHON_IMAGE_TYPE = "Python";

    private final DockerClient dockerClient;

    private final String pythonImageId;

    public DockerWorkerContainerManager(final DockerClient dockerClient) throws URISyntaxException {
        this.dockerClient = Objects.requireNonNull(dockerClient);
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
    }

    @Override
    public Worker runScript(final WorkerId workerId, final String script) throws UnableToRunScriptException {
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

            // start it in asynchronous way
            dockerClient.startContainerCmd(workerContainer.getId()).exec();
            LOGGER.info(String.format("Container workerId '%s' started.", workerId.id()));

            final InspectContainerResponse container = inspectWorkerContainer(workerId).get();
            final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.fromDockerStatus(container.getState().getStatus());
            return NewDockerContainerWorker.newBuilder()
                    .withWorkerId(workerId)
                    .withWorkerStatus(dockerContainerStatus.toWorkerStatus())
                    .withSource(Source.CONTAINER)
                    .withLastUpdateStateDate(LocalDateTime.now(ZoneOffset.UTC))
                    .withCreatedAt(ZonedDateTime.parse(container.getCreated()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                    .build();
        } catch (final IOException ioException) {
            throw new UnableToRunScriptException(workerId, ioException);
        }
    }

    @Override
    public List<? extends Worker> listAllContainers() {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> container.getLabels() != null)
                .filter(container -> container.getLabels().containsKey(WORKER_ID))
                .map(container -> WorkerDockerContainer.newBuilder()
                        .withWorkerId(new WorkerId(container.getLabels().get(WORKER_ID)))
                        .withWorkerStatus(DockerContainerStatus.fromDockerStatus(container.getState()).toWorkerStatus())
                        .withCreatedAt(LocalDateTime.ofEpochSecond(container.getCreated(), 0, ZoneOffset.UTC))
                        .withLastUpdateStateDate(LocalDateTime.now(ZoneOffset.UTC))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Worker> findContainer(final WorkerId workerId) {
        return inspectWorkerContainer(workerId)
                .map(container -> WorkerDockerContainer.newBuilder()
                        .withWorkerId(workerId)
                        .withWorkerStatus(DockerContainerStatus.fromDockerStatus(container.getState().getStatus()).toWorkerStatus())
                        .withCreatedAt(ZonedDateTime.parse(container.getCreated()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                        .withLastUpdateStateDate(LocalDateTime.now(ZoneOffset.UTC))
                        .build());
    }

    @Override
    public DockerContainerInformation getContainerMetadata(final WorkerId workerId) throws UnknownWorkerException {
        return inspectWorkerContainer(workerId)
                .map(DockerContainerInformation::new)
                .orElseThrow(() -> new UnknownWorkerException(workerId));
    }

    @Override
    public void deleteContainer(final WorkerId workerId) throws UnknownWorkerException {
        inspectWorkerContainer(workerId)
                .map(inspectContainerResponse -> {
                    dockerClient.removeContainerCmd(inspectContainerResponse.getId())
                            .withForce(true)
                            .exec();
                    return inspectContainerResponse;
                })
                .orElseThrow(() -> new UnknownWorkerException(workerId));
    }

    @Override
    public Optional<WorkerLog> getStdOut(final WorkerId workerId) {
        return inspectWorkerContainer(workerId)
                .map(inspectContainerResponse -> {
                    try {
                        final LoggingResultCallbackAdapter loggingResultCallbackAdapter = new LoggingResultCallbackAdapter();
                        dockerClient
                                .logContainerCmd(inspectContainerResponse.getId())
                                .withStdErr(false)
                                .withStdOut(true)
                                .withFollowStream(false)
                                .withTailAll()
                                .exec(loggingResultCallbackAdapter);
                        loggingResultCallbackAdapter.awaitCompletion();
                        final String log = loggingResultCallbackAdapter
                                .getStdResponses()
                                .stream()
                                .map(LoggingResultCallbackAdapter.StdResponse::getResponse)
                                .collect(Collectors.joining());
                        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                                .withWorkerId(workerId)
                                .withWorkerStatus(DockerContainerStatus.fromDockerStatus(inspectContainerResponse.getState().getStatus()).toWorkerStatus())
                                .withCreatedAt(ZonedDateTime.parse(inspectContainerResponse.getCreated()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                                .withLastUpdateStateDate(LocalDateTime.now(ZoneOffset.UTC))
                                .build();
                        return new WorkerLogDockerContainer(workerDockerContainer, log);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Optional<WorkerLog> getStdErr(final WorkerId workerId) {
        return inspectWorkerContainer(workerId)
                .map(inspectContainerResponse -> {
                    try {
                        final LoggingResultCallbackAdapter loggingResultCallbackAdapter = new LoggingResultCallbackAdapter();
                        dockerClient
                                .logContainerCmd(inspectContainerResponse.getId())
                                .withStdErr(true)
                                .withStdOut(false)
                                .withFollowStream(false)
                                .withTailAll()
                                .exec(loggingResultCallbackAdapter);
                        loggingResultCallbackAdapter.awaitCompletion();
                        final String log = loggingResultCallbackAdapter
                                .getStdResponses()
                                .stream()
                                .map(LoggingResultCallbackAdapter.StdResponse::getResponse)
                                .collect(Collectors.joining());
                        final WorkerDockerContainer workerDockerContainer = WorkerDockerContainer.newBuilder()
                                .withWorkerId(workerId)
                                .withWorkerStatus(DockerContainerStatus.fromDockerStatus(inspectContainerResponse.getState().getStatus()).toWorkerStatus())
                                .withCreatedAt(ZonedDateTime.parse(inspectContainerResponse.getCreated()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                                .withLastUpdateStateDate(LocalDateTime.now(ZoneOffset.UTC))
                                .build();
                        return new WorkerLogDockerContainer(workerDockerContainer, log);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
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
