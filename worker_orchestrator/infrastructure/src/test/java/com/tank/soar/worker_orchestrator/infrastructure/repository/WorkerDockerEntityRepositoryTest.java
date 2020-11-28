package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.infrastructure.WorkerLockMechanism;
import com.tank.soar.worker_orchestrator.infrastructure.container.NewWorkerDockerEvent;
import com.tank.soar.worker_orchestrator.infrastructure.container.StdResponse;
import com.tank.soar.worker_orchestrator.resources.PostgresqlTestResource;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
            stmt.executeUpdate("TRUNCATE TABLE WORKER_EVENT");
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
                 final ResultSet rsDockerStateSnapshot = stmt.executeQuery("SELECT * FROM WORKER_EVENT")) {
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
             final ResultSet rsWorker = stmt.executeQuery("SELECT * FROM WORKER");
             final ResultSet rsWorkerEvent = stmt.executeQuery("SELECT * FROM WORKER_EVENT")) {

            rsWorker.next();
            assertThat(rsWorker.getString("workerId")).isEqualTo("id");
            assertThat(rsWorker.getString("script")).isEqualTo("print(\"hello world\")");

            rsWorkerEvent.next();
            assertThat(rsWorkerEvent.getString("workerId")).isEqualTo("id");
            assertThat(rsWorkerEvent.getString("eventType")).isEqualTo("USER");
            assertThat(rsWorkerEvent.getObject("eventDate", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));
            assertThat(rsWorkerEvent.getString("zoneOffset")).isEqualTo("Z");
            assertThat(rsWorkerEvent.getString("container")).isNull();
            assertThat(rsWorkerEvent.getString("logStreams")).isNull();
            assertThat(rsWorkerEvent.getString("userEventType")).isEqualTo("CREATION_REQUESTED");
        }
    }

    @Test
    @Order(3)
    public void should_create_worker_rollback_worker_when_worker_event_already_exist() throws SQLException {
        // Given
        final WorkerId workerId = new WorkerId("id");
        final UTCZonedDateTime utcZonedDateTime = UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00);
        try (final Connection con = workerDataSource.getConnection();
             final PreparedStatement createUserWorkerEventPreparedStatement = con.prepareStatement(WorkerDockerEntityRepository.CREATE_USER_WORKER_EVENT)) {
            createUserWorkerEventPreparedStatement.setString(1, workerId.id());
            createUserWorkerEventPreparedStatement.setString(2, "CREATION_REQUESTED");
            createUserWorkerEventPreparedStatement.setObject(3, utcZonedDateTime.localDateTime());
            createUserWorkerEventPreparedStatement.setString(4, utcZonedDateTime.zoneOffset().getId());
            Validate.validState(createUserWorkerEventPreparedStatement.executeUpdate() == 1);
        }

        // When
        try {
            workerDockerEntityRepository.createWorker(workerId, "print(\"hello world\")", utcZonedDateTime);
            fail("should have failed !");
        } catch (final Exception e) {

        }

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet countWorkerRS = stmt.executeQuery("SELECT COUNT(*) FROM WORKER");
             final ResultSet countWorkerEventsRS = stmt.executeQuery("SELECT COUNT(*) FROM WORKER_EVENT")) {
            countWorkerRS.next();
            assertThat(countWorkerRS.getLong(1)).isEqualTo(0l);
            countWorkerEventsRS.next();
            assertThat(countWorkerEventsRS.getLong(1)).isEqualTo(1l);// I've got one in my given
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
                .withWorkerEventEntities(Collections.singletonList(
                        new WorkerEventEntity(
                                new WorkerId("id"),
                                EventType.USER,
                                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                                null,
                                UserEventType.CREATION_REQUESTED
                        )))
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
                .withWorkerEventEntities(Collections.singletonList(
                        new WorkerEventEntity(
                                new WorkerId("id"),
                                EventType.USER,
                                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                                null,
                                UserEventType.CREATION_REQUESTED
                        )))
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
    @Order(11)
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
    Event<NewWorkerDockerEvent> dockerStateChangedEvent;

    @Test
    @Order(12)
    public void should_store_docker_state_on_docker_state_changed() throws Exception {
        // Given
        final InspectContainerResponse inspectContainerResponse = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)// Some properties are not supported by docker java api
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .readValue(this.getClass().getResourceAsStream("/given_container_inspect.json"),
                        new TypeReference<InspectContainerResponse>() {});

        // When
        dockerStateChangedEvent.fire(NewWorkerDockerEvent.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(inspectContainerResponse)
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
            final ResultSet resultSet = stmt.executeQuery("SELECT workerId, eventType, eventDate, zoneOffset, container, logStreams, userEventType FROM WORKER_EVENT");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("workerId")).isEqualTo("id");
            assertThat(resultSet.getString("eventType")).isEqualTo(EventType.DOCKER.name());
            assertThat(resultSet.getString("eventDate")).isEqualTo("2020-09-01 10:00:00");
            assertThat(resultSet.getString("zoneOffset")).isEqualTo("Z");
            final String expectedContainer = new Scanner(getClass().getResourceAsStream("/expected_container_inspect.json")).useDelimiter("\\A").next();
            JSONAssert.assertEquals(expectedContainer, resultSet.getString("container"), JSONCompareMode.NON_EXTENSIBLE);
            assertThat(resultSet.getString("logStreams")).isEqualTo("[{\"content\": \"stdOut\", \"workerId\": \"id\", \"logStreamType\": \"STDOUT\"}, {\"content\": \"stdErr\", \"workerId\": \"id\", \"logStreamType\": \"STDERR\"}]");
            assertThat(resultSet.getString("userEventType")).isNull();
        }
    }

    @Test
    @Order(13)
    public void should_store_docker_state_use_locking_mechanism() throws Exception {
        // Given
        final InspectContainerResponse inspectContainerResponse = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .readValue(this.getClass().getResourceAsStream("/given_container_inspect.json"),
                        new TypeReference<InspectContainerResponse>() {});
        final InOrder inOrder = inOrder(workerLockMechanism);

        // When
        dockerStateChangedEvent.fire(NewWorkerDockerEvent.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(inspectContainerResponse)
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Collections.emptyList())
                .build());

        // Then
        inOrder.verify(workerLockMechanism).lock(new WorkerId("id"));
        inOrder.verify(workerLockMechanism).unlock(new WorkerId("id"));
    }

    @Test
    @Order(14)
    public void should_store_docker_state_be_unlocked_when_exception_is_thrown() throws Exception {
        // Given
        final InspectContainerResponse inspectContainerResponse = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .readValue(this.getClass().getResourceAsStream("/given_container_inspect.json"),
                        new TypeReference<InspectContainerResponse>() {});
        final NewWorkerDockerEvent newWorkerDockerEvent = NewWorkerDockerEvent.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(inspectContainerResponse)
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withStdResponses(Collections.emptyList())
                .build();
        dockerStateChangedEvent.fire(newWorkerDockerEvent);
        reset(workerLockMechanism);

        // When
        try {
            dockerStateChangedEvent.fire(newWorkerDockerEvent);
            fail("should have failed ! due to the primary key on workerId and snapshotDate");
        } catch (final Exception e) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    private void givenWorkerLog() throws Exception {
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        final InspectContainerResponse inspectContainerResponse = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .readValue(this.getClass().getResourceAsStream("/given_container_inspect.json"),
                        new TypeReference<InspectContainerResponse>() {});

        dockerStateChangedEvent.fire(NewWorkerDockerEvent.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withContainer(inspectContainerResponse)
                .withDockerStateChangedDate(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00))
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
    }

    @Test
    @Order(15)
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
    @Order(16)
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

    @Test
    @Order(17)
    public void should_mark_worker_as_manually_stopped() throws Exception {
        // Given
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // When
        final Worker worker = workerDockerEntityRepository.markWorkerAsManuallyStopped(new WorkerId("id"),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00));

        // Then
        assertThat(worker).isEqualTo(WorkerDockerEntity
                .newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withWorkerEventEntities(Arrays.asList(
                        new WorkerEventEntity(
                                new WorkerId("id"),
                                EventType.USER,
                                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00),
                                null,
                                UserEventType.CREATION_REQUESTED
                        ),
                        new WorkerEventEntity(
                                new WorkerId("id"),
                                EventType.USER,
                                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00),
                                null,
                                UserEventType.WORKER_MANUALLY_STOPPED
                        )))
                .build());
    }

    @Test
    @Order(18)
    public void should_mark_worker_as_manually_stopped_use_locking_mechanism() throws Exception {
        // Given
        final InOrder inOrder = inOrder(workerLockMechanism);
        workerDockerEntityRepository.createWorker(new WorkerId("id"), "print(\"hello world\")",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));

        // When
        workerDockerEntityRepository.markWorkerAsManuallyStopped(new WorkerId("id"),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00));

        // Then
        inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        // I do not know how to check that the query has been done between
        inOrder.verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(19)
    public void should_mark_worker_as_manually_stopped_throw_unknown_worker_exception_when_worker_does_not_exist() {
        // Given

        // When && Then
        assertThatThrownBy(() -> workerDockerEntityRepository.markWorkerAsManuallyStopped(new WorkerId("id"),
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00))
        )
                .isInstanceOf(UnknownWorkerException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    @Order(20)
    public void should_mark_worker_as_manually_stopped_be_unlocked_when_worker_does_not_exist() {
        // Given

        // When
        try {
            workerDockerEntityRepository.markWorkerAsManuallyStopped(new WorkerId("id"),
                    UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00));
            fail("should have failed !");
        } catch (final UnknownWorkerException unknownWorkerException) {

        }

        // Then
        verify(workerLockMechanism, times(1)).unlock(new WorkerId("id"));
    }

    @Test
    @Order(21)
    public void should_mark_worker_as_manually_stopped_do_not_add_event_when_worker_does_not_exist() throws Exception {
        // Given

        // When
        try {
            workerDockerEntityRepository.markWorkerAsManuallyStopped(new WorkerId("id"),
                    UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00));
            fail("should have failed !");
        } catch (final UnknownWorkerException unknownWorkerException) {

        }

        // Then
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet countWorkerEventsRS = stmt.executeQuery("SELECT COUNT(*) FROM WORKER_EVENT")) {
            countWorkerEventsRS.next();
            assertThat(countWorkerEventsRS.getLong(1)).isEqualTo(0l);
        }
    }

}
