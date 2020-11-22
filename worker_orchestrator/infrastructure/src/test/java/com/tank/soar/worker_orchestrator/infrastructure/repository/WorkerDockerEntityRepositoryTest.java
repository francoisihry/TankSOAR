package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerStateChanged;
import com.tank.soar.worker_orchestrator.infrastructure.container.StdResponse;
import com.tank.soar.worker_orchestrator.resources.PostgresqlTestResource;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(PostgresqlTestResource.class)
public class WorkerDockerEntityRepositoryTest {

    @Inject
    @DataSource("workers")
    AgroalDataSource workerDataSource;

    @InjectSpy
    WorkerLockMechanism workerLockMechanism;

    @Inject
    WorkerDockerEntityRepository workerDockerEntityRepository;

    @BeforeEach
    @AfterEach
    public void flushAll() {
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE WORKER");
            stmt.executeUpdate("TRUNCATE TABLE DOCKER_STATE_SNAPSHOT");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    public void should_tables_be_created_at_startup() {
        assertThatCode(() -> {
            try (final Connection con = workerDataSource.getConnection();
                 final Statement stmt = con.createStatement();
                 final ResultSet rsWorker = stmt.executeQuery("SELECT * FROM WORKER");
                 final ResultSet rsDockerStateSnapshot = stmt.executeQuery("SELECT * FROM DOCKER_STATE_SNAPSHOT")) {
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Order(2)
    public void should_create_worker() throws SQLException {
        // Given

        // When
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet rsWorker = stmt.executeQuery("SELECT * FROM WORKER")) {

            rsWorker.next();
            assertThat(rsWorker.getString("workerId")).isEqualTo("id");
            assertThat(rsWorker.getString("script")).isEqualTo("print(\"hello world\")");
            assertThat(rsWorker.getObject("createdAt", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));
            assertThat(rsWorker.getString("zoneOffset")).isEqualTo("Z");
        }
    }

    @Test
    @Order(4)
    public void should_list_all_workers() {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // When
        final List<? extends Worker> workers = workerDockerEntityRepository.listAllWorkers();

        // Then
        assertThat(workers).hasSize(1);
        assertThat(workers.get(0)).isEqualTo(WorkerDockerEntity
                .newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(Collections.emptyList())
                .build());
    }

    @Test
    @Order(5)
    public void should_get_worker() throws Exception {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // When
        final Worker worker = workerDockerEntityRepository.getWorker(new WorkerId("id"));

        // Then
        assertThat(worker).isEqualTo(WorkerDockerEntity
                .newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(Collections.emptyList())
                .build());
    }

    @Test
    @Order(6)
    public void should_get_worker_throw_exception_when_worker_does_not_exist() {
        // Given

        // When && Then
        assertThatCode(() -> {
            workerDockerEntityRepository.getWorker(new WorkerId("id"));
        })
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(7)
    public void should_get_std_out_log() throws Exception {
        // Given
        givenWorkerLog();

        // When
        final List<? extends LogStream> workerLog = workerDockerEntityRepository.getLog(new WorkerId("id"), Boolean.TRUE, Boolean.FALSE);

        // Then
        assertThat(workerLog).hasSize(1);
        assertThat(workerLog.get(0)).isEqualTo(new LogStreamEntity(new WorkerId("id"), LogStreamType.STDOUT, "stdOut"));
    }

    @Test
    @Order(8)
    public void should_get_std_out_log_throw_exception_when_worker_does_not_exist() {
        // Given

        // When && Then
        assertThatCode(() -> workerDockerEntityRepository.getLog(new WorkerId("id"), Boolean.TRUE, Boolean.FALSE))
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(9)
    public void should_get_std_err_log() throws Exception {
        // Given
        givenWorkerLog();

        // When
        final List<? extends LogStream> workerLog = workerDockerEntityRepository.getLog(new WorkerId("id"), Boolean.FALSE, Boolean.TRUE);

        // Then
        assertThat(workerLog).hasSize(1);
        assertThat(workerLog.get(0)).isEqualTo(new LogStreamEntity(new WorkerId("id"), LogStreamType.STDERR, "stdErr"));
    }

    @Test
    @Order(10)
    public void should_get_std_err_log_throw_exception_when_worker_does_not_exist() {
        // Given

        // When && Then
        assertThatCode(() -> workerDockerEntityRepository.getLog(new WorkerId("id"), Boolean.FALSE, Boolean.TRUE))
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(9)
    public void should_get_std_out_and_std_err_logs_in_order() throws Exception {
        // Given
        givenWorkerLog();

        // When
        final List<? extends LogStream> workerLog = workerDockerEntityRepository.getLog(new WorkerId("id"), Boolean.TRUE, Boolean.TRUE);

        // Then
        assertThat(workerLog).hasSize(2);
        assertThat(workerLog.get(0)).isEqualTo(new LogStreamEntity(new WorkerId("id"), LogStreamType.STDOUT, "stdOut"));
        assertThat(workerLog.get(1)).isEqualTo(new LogStreamEntity(new WorkerId("id"), LogStreamType.STDERR, "stdErr"));
    }

    @Inject
    Event<DockerStateChanged> dockerStateChangedEvent;

    @Test
    @Order(10)
    public void should_store_docker_state_on_docker_state_changed() throws SQLException {
        // Given

        // When
        dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(new InspectContainerResponse())
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Arrays.asList(
                        StdResponse.newBuilder()
                                .withWorkerId(new WorkerId("id"))
                                .withLogStreamType(LogStreamType.STDOUT)
                                .withContent("stdOut")
                                .build(),
                        StdResponse.newBuilder()
                                .withWorkerId(new WorkerId("id"))
                                .withLogStreamType(LogStreamType.STDERR)
                                .withContent("stdErr")
                                .build()))
                .build());

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement()) {
            final ResultSet resultSet = stmt.executeQuery("SELECT workerId, container, logStreams, snapshotDate, zoneOffset FROM DOCKER_STATE_SNAPSHOT");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("workerId")).isEqualTo("id");
            assertThat(resultSet.getString("container")).isEqualTo("{\"Id\": null, \"Args\": null, \"Name\": null, \"Node\": null, \"Path\": null, \"Image\": null, \"State\": null, \"Config\": null, \"Driver\": null, \"Mounts\": null, \"Created\": null, \"ExecIDs\": null, \"LogPath\": null, \"Volumes\": null, \"Platform\": null, \"HostsPath\": null, \"VolumesRW\": null, \"ExecDriver\": null, \"HostConfig\": null, \"MountLabel\": null, \"SizeRootFs\": null, \"GraphDriver\": null, \"HostnamePath\": null, \"ProcessLabel\": null, \"RestartCount\": null, \"ResolvConfPath\": null, \"NetworkSettings\": null}");
            assertThat(resultSet.getString("logStreams")).isEqualTo("[{\"content\": \"stdOut\", \"workerId\": \"id\", \"logStreamType\": \"STDOUT\"}, {\"content\": \"stdErr\", \"workerId\": \"id\", \"logStreamType\": \"STDERR\"}]");
            assertThat(resultSet.getString("snapshotDate")).isEqualTo("2020-09-01 10:00:00");
            assertThat(resultSet.getString("zoneOffset")).isEqualTo("Z");
        }
    }

    @Test
    @Order(11)
    public void should_store_docker_state_use_locking_mechanism() throws SQLException {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism);

        // When
        dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(new InspectContainerResponse())
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Collections.emptyList())
                .build());

        // Then
        inOrder.verify(workerLockMechanism).lock(new WorkerId("id"));
        inOrder.verify(workerLockMechanism).unlock(new WorkerId("id"));
    }

    @Test
    @Order(12)
    public void should_store_docker_state_be_unlocked_when_exception_is_thrown() {
        // Given
        final DockerStateChanged dockerStateChanged = DockerStateChanged.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(new InspectContainerResponse())
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Collections.emptyList())
                .build();
        dockerStateChangedEvent.fire(dockerStateChanged);
        reset(workerLockMechanism);

        // When
        try {
            dockerStateChangedEvent.fire(dockerStateChanged);
            fail("should have failed ! due to the primary key on workerId and snapshotDate");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    private void givenWorkerLog() {
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        final StdResponse stdResponseStdout = StdResponse.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withLogStreamType(LogStreamType.STDOUT)
                .withContent("stdOut")
                .build();
        final StdResponse stdResponseStderr = StdResponse.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withLogStreamType(LogStreamType.STDERR)
                .withContent("stdErr")
                .build();
        dockerStateChangedEvent.fire(DockerStateChanged.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(new InspectContainerResponse())
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Arrays.asList(stdResponseStdout, stdResponseStderr))
                .build());
    }

    @Test
    @Order(14)
    public void should_create_worker_use_locking_mechanism() {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism);

        // When
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // Then
        inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        // I do not know how to check that the query has been done between
        inOrder.verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(15)
    public void should_create_worker_be_unlocked_when_exception_is_thrown() {
        // Given

        // When
        try {
            workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                    null);
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

}
