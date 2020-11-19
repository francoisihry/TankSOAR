package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
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
                 final ResultSet rsConsumedEvent = stmt.executeQuery("SELECT * FROM WORKER")) {
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
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet rsWorker = stmt.executeQuery("SELECT * FROM WORKER")) {

            rsWorker.next();
            assertThat(rsWorker.getString("workerId")).isEqualTo("id");
            assertThat(rsWorker.getString("script")).isEqualTo("print(\"hello world\")");
            assertThat(rsWorker.getString("workerStatus")).isEqualTo("CREATING");
            assertThat(rsWorker.getObject("createdAt", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));
            assertThat(rsWorker.getObject("lastUpdateStateDate", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));
            assertThat(rsWorker.getString("zoneOffset")).isEqualTo("Z");
            assertThat(rsWorker.getString("container")).isNull();
            assertThat(rsWorker.getString("logStreams")).isNull();
        }
    }

    @Test
    @Order(3)
    public void should_update_worker() throws SQLException {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.RUNNING)
                .when(worker).workerStatus();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00))
                .when(worker).lastUpdateStateDate();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 11, 00))
                .when(worker).createdAt();
        final ContainerInformation containerInformation = mock(ContainerInformation.class);
        doReturn("{\"hello\":\"world\"}").when(containerInformation).fullInformation();
        final LogStream logStream = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStream).workerId();
        doReturn("hello world").when(logStream).content();
        doReturn(LogStreamType.STDOUT).when(logStream).logStreamType();
        doCallRealMethod().when(logStream).toJsonStringRepresentation();

        // When
        workerDockerEntityRepository.saveWorker(worker, containerInformation, Collections.singletonList(logStream));

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet rsWorker = stmt.executeQuery("SELECT * FROM WORKER")) {

            rsWorker.next();
            assertThat(rsWorker.getString("workerId")).isEqualTo("id");
            assertThat(rsWorker.getString("script")).isEqualTo("print(\"hello world\")");
            assertThat(Arrays.asList(rsWorker.getString("workerStatus"))).containsAnyOf("FINISHED", "RUNNING");
            assertThat(rsWorker.getObject("lastUpdateStateDate", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00));
            assertThat(rsWorker.getObject("createdAt", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 11, 00));
            assertThat(rsWorker.getString("zoneOffset")).isEqualTo("Z");
            assertThat(rsWorker.getString("container")).isEqualTo("{\"hello\": \"world\"}");
            assertThat(rsWorker.getString("logStreams")).isEqualTo("[{\"content\": \"hello world\", \"workerId\": \"id\", \"logStreamType\": \"STDOUT\"}]");
        }
    }

    @Test
    @Order(4)
    public void should_list_all_workers() {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));

        // When
        final List<? extends Worker> workers = workerDockerEntityRepository.listAllWorkers();

        // Then
        assertThat(workers).hasSize(1);
        assertThat(workers.get(0)).isEqualTo(new WorkerDockerEntity(new WorkerId("id"),
                WorkerStatus.CREATING,
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)));
    }

    @Test
    @Order(5)
    public void should_get_worker() throws Exception {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));

        // When
        final Worker worker = workerDockerEntityRepository.getWorker(new WorkerId("id"));

        // Then
        assertThat(worker).isEqualTo(new WorkerDockerEntity(new WorkerId("id"),
                WorkerStatus.CREATING,
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)));
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

    private void givenWorkerLog() {
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.RUNNING)
                .when(worker).workerStatus();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00))
                .when(worker).lastUpdateStateDate();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 11, 00))
                .when(worker).createdAt();
        final ContainerInformation containerInformation = mock(ContainerInformation.class);
        doReturn("{\"hello\":\"world\"}").when(containerInformation).fullInformation();


        final LogStream logStreamStdOut = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdOut).workerId();
        doReturn("stdOut").when(logStreamStdOut).content();
        doReturn(LogStreamType.STDOUT).when(logStreamStdOut).logStreamType();
        doCallRealMethod().when(logStreamStdOut).toJsonStringRepresentation();
        final LogStream logStreamStdErr = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdErr).workerId();
        doReturn("stdErr").when(logStreamStdErr).content();
        doReturn(LogStreamType.STDERR).when(logStreamStdErr).logStreamType();
        doCallRealMethod().when(logStreamStdErr).toJsonStringRepresentation();

        workerDockerEntityRepository.saveWorker(worker, containerInformation,
                Arrays.asList(logStreamStdOut, logStreamStdErr));
    }

    @Test
    @Order(12)
    public void should_has_worker_return_true_when_the_worker_is_present() {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));

        // When
        final boolean hasWorker = workerDockerEntityRepository.hasWorker(new WorkerId("id"));

        // Then
        assertThat(hasWorker).isTrue();
    }

    @Test
    @Order(13)
    public void should_has_worker_return_false_when_the_worker_is_not_present() {
        // Given

        // When
        final boolean hasWorker = workerDockerEntityRepository.hasWorker(new WorkerId("id"));

        // Then
        assertThat(hasWorker).isFalse();
    }

    @Test
    @Order(14)
    public void should_create_worker_use_locking_mechanism() {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism);

        // When
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));

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
                    UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                    null);
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(16)
    public void should_save_worker_use_locking_mechanism() {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism);
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00));
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.RUNNING)
                .when(worker).workerStatus();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00))
                .when(worker).lastUpdateStateDate();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 11, 00))
                .when(worker).createdAt();
        final ContainerInformation containerInformation = mock(ContainerInformation.class);
        doReturn("{\"hello\":\"world\"}").when(containerInformation).fullInformation();
        final List<? extends LogStream> logStreams = Collections.emptyList();

        // When
        workerDockerEntityRepository.saveWorker(worker, containerInformation, logStreams);

        // Then
        inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        // I do not know how to check that the query has been done between
        inOrder.verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(17)
    public void should_save_worker_be_unlocked_when_exception_is_thrown() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        final ContainerInformation containerInformation = mock(ContainerInformation.class);
        final List<? extends LogStream> logStreams = Collections.emptyList();

        // When
        try {
            workerDockerEntityRepository.saveWorker(worker, containerInformation, logStreams);
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

}
