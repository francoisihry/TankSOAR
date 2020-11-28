package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import com.github.dockerjava.api.DockerClient;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerIdProvider;
import com.tank.soar.worker_orchestrator.domain.usecase.RunScriptCommand;
import com.tank.soar.worker_orchestrator.domain.usecase.RunScriptUseCase;
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
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
public class LoggingFrameSocketTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFrameSocketTest.class);

    private static final LinkedBlockingDeque<String> JSON_LOG_MESSAGES = new LinkedBlockingDeque<>();

    @TestHTTPResource("/logs/id")
    URI logsUri;

    @Inject
    RunScriptUseCase runScriptUseCase;

    @Inject
    @DataSource("workers")
    AgroalDataSource workerDataSource;

    @Inject
    DockerClient dockerClient;

    @InjectMock
    WorkerIdProvider workerIdProvider;

    @BeforeEach
    public void setup() {
        doReturn(new WorkerId("id")).when(workerIdProvider).provideNewWorkerId();
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
            stmt.executeUpdate("TRUNCATE TABLE WORKER_EVENT");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_consume_run_script_logs() throws Exception {
        // Given
        final RunScriptCommand command = RunScriptCommand.newBuilder().withScript(
                Stream.of("import sys;",
                        "import time;",
                        "print('hello');",
                        "time.sleep(3);",
                        "print('bye bye world', file=sys.stderr);")
                        .collect(Collectors.joining())).build();

        // When
        runScriptUseCase.execute(command);

        // Then
        try (final Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, logsUri)) {
            assertThat(JSON_LOG_MESSAGES.poll(10, TimeUnit.SECONDS))
                    .isEqualTo("{\"workerId\":\"id\",\"logStreamType\":\"STDOUT\",\"content\":\"hello\"}");
            TimeUnit.SECONDS.sleep(3);
            assertThat(JSON_LOG_MESSAGES.poll(1, TimeUnit.SECONDS))
                    .isEqualTo("{\"workerId\":\"id\",\"logStreamType\":\"STDERR\",\"content\":\"bye bye world\"}");
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(final Session session) {
            // Nothing to do
        }

        @OnMessage
        void message(final String jsonLogMessage) {
            JSON_LOG_MESSAGES.add(jsonLogMessage);
        }

    }

}
