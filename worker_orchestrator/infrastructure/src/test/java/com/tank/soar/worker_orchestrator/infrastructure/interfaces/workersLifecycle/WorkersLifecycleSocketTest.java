package com.tank.soar.worker_orchestrator.infrastructure.interfaces.workersLifecycle;

import com.github.dockerjava.api.DockerClient;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerIdProvider;
import com.tank.soar.worker_orchestrator.domain.usecase.RunScriptCommand;
import com.tank.soar.worker_orchestrator.domain.usecase.RunScriptUseCase;
import com.tank.soar.worker_orchestrator.infrastructure.UTCZonedDateTimeProvider;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerLastUpdateStateDateProvider;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerWorkerContainerManager;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Month;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
public class WorkersLifecycleSocketTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkersLifecycleSocketTest.class);

    private static final LinkedBlockingDeque<String> JSON_WORKER_LIFECYCLE_MESSAGE = new LinkedBlockingDeque<>();

    @TestHTTPResource("/workersLifecycle")
    URI workersLifecycleUri;

    @Inject
    @DataSource("workers")
    AgroalDataSource workerDataSource;

    @Inject
    DockerClient dockerClient;

    @Inject
    RunScriptUseCase runScriptUseCase;

    @InjectMock
    WorkerIdProvider workerIdProvider;

    @InjectMock
    UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    @InjectMock
    DockerLastUpdateStateDateProvider dockerLastUpdateStateDateProvider;

    @BeforeEach
    public void setup() {
        doReturn(new WorkerId("id")).when(workerIdProvider).provideNewWorkerId();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00)).when(utcZonedDateTimeProvider).now();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 01))
                .doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 02))
                .doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 03))
                .when(dockerLastUpdateStateDateProvider).lastUpdateStateDate(any());
    }

    @BeforeEach
    @AfterEach
    public void removeWorkerContainer() {
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
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE WORKER");
            stmt.executeUpdate("TRUNCATE TABLE DOCKER_STATE_SNAPSHOT");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_consume_worker_lifecycle() throws Exception {
        // Given
        final RunScriptCommand command = RunScriptCommand.newBuilder().withScript(
                Stream.of("print('hello');")
                        .collect(Collectors.joining())).build();

        // When
        // Without executing it in an another thread I we will not be able to have the first message because
        // we are using the same thread than the client.
        Executors.newSingleThreadExecutor().submit(() -> runScriptUseCase.execute(command));

        // Then
        try (final Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, workersLifecycleUri)) {
            assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS))
                    .isEqualTo("{\"workerId\":\"id\",\"workerStatus\":\"CREATED\",\"lastUpdateStateDate\":\"2020-09-01T10:10:01Z\", \"hasFinished\": \"false\"}");
            assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS))
                    .isEqualTo("{\"workerId\":\"id\",\"workerStatus\":\"RUNNING\",\"lastUpdateStateDate\":\"2020-09-01T10:10:02Z\", \"hasFinished\": \"false\"}");
            assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS))
                    .isEqualTo("{\"workerId\":\"id\",\"workerStatus\":\"FINISHED\",\"lastUpdateStateDate\":\"2020-09-01T10:10:03Z\", \"hasFinished\": \"true\"}");
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(final Session session) {
            // Nothing to do
        }

        @OnMessage
        public void message(final String jsonWorkerLifecycleMessage) {
            JSON_WORKER_LIFECYCLE_MESSAGE.add(jsonWorkerLifecycleMessage);
        }

    }

}
