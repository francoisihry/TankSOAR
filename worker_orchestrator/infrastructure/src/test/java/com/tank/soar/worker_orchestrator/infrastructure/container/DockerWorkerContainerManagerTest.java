package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.DockerClient;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@QuarkusTest
public class DockerWorkerContainerManagerTest {

    @Inject
    DockerWorkerContainerManager dockerWorkerContainerManager;

    @InjectSpy
    WorkerLockMechanism workerLockMechanism;

    @InjectSpy
    DockerClient dockerClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWorkerContainerManagerTest.class);

    @AfterEach
    @BeforeEach
    public void removeWorkersContainers() {
        dockerClient.listContainersCmd()
                .withLabelFilter(Collections.singleton(DockerWorkerContainerManager.WORKER_ID))
                .withShowAll(true)
                .exec()
                .stream()
                .peek(container -> LOGGER.info(String.format("Need to remove container '%s'", container.getId())))
                .forEach(container -> {
                        try {
                            dockerClient.removeContainerCmd(container.getId())
                                    .withForce(true)
                                    .exec();
                        } catch (final Exception e) {}
                });
    }

    @Test
    @Order(1)
    public void should_have_build_python_image_at_startup() {
        // When && Then
        assertThat(dockerClient.listImagesCmd()
                .withLabelFilter(Collections.singletonMap(DockerWorkerContainerManager.IMAGE_TYPE,
                        DockerWorkerContainerManager.PYTHON_IMAGE_TYPE))
                .exec()
                .stream().count()).isEqualTo(1l);
    }

    @Test
    @Order(2)
    public void should_run_script_create_a_worker_container() throws Exception {
        // Given

        // When
        final Worker worker = dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // Then
        assertThat(worker.workerId()).isEqualTo(new WorkerId("id"));
        assertThat(worker.source()).isEqualTo(Source.CONTAINER);
        assertThat(worker.workerStatus()).isNotNull();
        assertThat(worker.createdAt()).isNotNull();
        assertThat(worker.lastUpdateStateDate()).isNotNull();
        assertThat(worker.hasFinished()).isNotNull();
        assertThat(dockerClient.listContainersCmd()
                .withLabelFilter(Collections.singleton(DockerWorkerContainerManager.WORKER_ID))
                .withShowAll(true)
                .exec()
                .stream()
                .count()).isEqualTo(1l);
    }

    @Test
    @Order(3)
    public void should_list_all_workers_containers() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // When
        final List<? extends Worker> workers = dockerWorkerContainerManager.listAllContainers();

        // Then
        assertThat(workers).hasSize(1);
        assertThat(workers.get(0).workerId()).isEqualTo(new WorkerId("id"));
    }

    @Test
    @Order(4)
    public void should_find_containers() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // When
        final Optional<Worker> worker = dockerWorkerContainerManager.findContainer(new WorkerId("id"));

        // Then
        assertThat(worker.isPresent()).isTrue();
        assertThat(worker.get().workerId()).isEqualTo(new WorkerId("id"));
    }

    @Test
    @Order(5)
    public void should_find_containers_return_optional_empty_on_unknown_worker() throws Exception {
        // Given

        // When
        final Optional<Worker> worker = dockerWorkerContainerManager.findContainer(new WorkerId("id"));

        // Then
        assertThat(worker.isPresent()).isFalse();
    }

    @Test
    @Order(6)
    public void should_get_container_metadata() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // When
        final DockerContainerInformation dockerContainerInformation = dockerWorkerContainerManager.getContainerMetadata(new WorkerId("id"));

        // Then
        assertThat(dockerContainerInformation.fullInformation()).isNotBlank();
    }

    @Test
    @Order(7)
    public void should_get_container_metadata_throw_unknown_worker_exception_on_unknown_worker() {
        // Given

        // When && Then
        assertThatCode(() -> dockerWorkerContainerManager.getContainerMetadata(new WorkerId("id")))
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(8)
    public void should_delete_container() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // When
        dockerWorkerContainerManager.deleteContainer(new WorkerId("id"));

        // Then
        assertThat(dockerClient.listContainersCmd()
                .withLabelFilter(Collections.singleton(DockerWorkerContainerManager.WORKER_ID))
                .withShowAll(true)
                .exec()
                .stream()
                .count()).isEqualTo(0l);
    }

    @Test
    @Order(7)
    public void should_delete_container_throw_unknown_worker_exception_on_unknown_worker() {
        // Given

        // When && Then
        assertThatCode(() -> dockerWorkerContainerManager.deleteContainer(new WorkerId("id")))
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(8)
    public void should_get_std_out_return_hello_world() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // When
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !"".equals(dockerWorkerContainerManager.getStdOut(new WorkerId("id")).get().log()));
        final Optional<WorkerLog> workerLog = dockerWorkerContainerManager.getStdOut(new WorkerId("id"));

        // Then

        assertThat(workerLog.isPresent()).isTrue();
        assertThat(workerLog.get().log()).isEqualTo("hello world\n");
        assertThat(workerLog.get().workerId()).isEqualTo(new WorkerId("id"));
        assertThat(workerLog.get().hasFinishedProducingLog()).isNotNull();
    }

    @Test
    @Order(9)
    public void should_get_std_out_return_optional_empty_on_unknown_worker() throws Exception {
        // Given

        // When
        final Optional<WorkerLog> workerLog = dockerWorkerContainerManager.getStdOut(new WorkerId("id"));

        // Then
        assertThat(workerLog.isPresent()).isFalse();
    }

    @Test
    @Order(10)
    public void should_get_std_err_return_bye_bye_world() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "import sys\nprint(\"bye bye world\", file=sys.stderr)");

        // When
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !"".equals(dockerWorkerContainerManager.getStdErr(new WorkerId("id")).get().log()));
        final Optional<WorkerLog> workerLog = dockerWorkerContainerManager.getStdErr(new WorkerId("id"));

        // Then
        assertThat(workerLog.isPresent()).isTrue();
        assertThat(workerLog.get().log()).isEqualTo("bye bye world\n");
        assertThat(workerLog.get().workerId()).isEqualTo(new WorkerId("id"));
        assertThat(workerLog.get().hasFinishedProducingLog()).isNotNull();
    }

    @Test
    @Order(11)
    public void should_get_std_err_return_optional_empty_on_unknown_worker() throws Exception {
        // Given

        // When
        final Optional<WorkerLog> workerLog = dockerWorkerContainerManager.getStdErr(new WorkerId("id"));

        // Then
        assertThat(workerLog.isPresent()).isFalse();
    }

    @Test
    @Order(12)
    public void should_run_script_use_locking_mechanism() throws Exception {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism, dockerClient);

        // When
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");

        // Then
        inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        verify(dockerClient, times(1)).startContainerCmd(any());
        inOrder.verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(13)
    public void should_run_script_be_unlocked_when_exception_is_thrown() {
        // Given
        doThrow(new RuntimeException()).when(dockerClient).startContainerCmd(anyString());

        // When
        try {
            dockerWorkerContainerManager.runScript(new WorkerId("id"), "print(\"hello world\")");
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(14)
    public void should_delete_container_use_locking_mechanism() throws Exception {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism, dockerClient);
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "import sys\nprint(\"bye bye world\", file=sys.stderr)");

        // When
        dockerWorkerContainerManager.deleteContainer(new WorkerId("id"));

        // then
        inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        verify(dockerClient, times(1)).removeContainerCmd(any());
        inOrder.verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(15)
    public void should_delete_container_unlocked_when_exception_is_thrown() throws Exception {
        // Given
        dockerWorkerContainerManager.runScript(new WorkerId("id"), "import sys\nprint(\"bye bye world\", file=sys.stderr)");
        doThrow(new RuntimeException()).when(dockerClient).removeContainerCmd(anyString());
        reset(workerLockMechanism);

        // When
        try {
            dockerWorkerContainerManager.deleteContainer(new WorkerId("id"));
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

}
