package com.tank.soar.worker_orchestrator.e2e.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EndToEndTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> postgresWorkersContainer;

    private GenericContainer<?> workerOrchestratorContainer;

    private final Network network = Network.newNetwork();

    @Override
    public Map<String, String> start() {
        final String applicationPort = "8080";
        postgresWorkersContainer = new PostgreSQLContainer<>("postgres:11-alpine")
                .withNetwork(network)
                .withNetworkAliases("worker")
                .withDatabaseName("worker")
                .withUsername("postgres")
                .withPassword("postgres");
        postgresWorkersContainer.start();
        System.setProperty("quarkus.datasource.workers.jdbc.url", postgresWorkersContainer.getJdbcUrl());
        System.setProperty("quarkus.datasource.workers.username", "postgres");
        System.setProperty("quarkus.datasource.workers.password", "postgres");
        /**
         * To be able to access the docker tcp localhost I need to run the container with the 'host' network.
         * In this case no mapping port. The port 8080 will stay fix (8080) on the host.
         */
        workerOrchestratorContainer = new GenericContainer<>("tanksoar/worker_orchestrator:latest-testing")
                .withEnv("JAVA_OPTIONS",
                        Stream.of(
                                "-Dquarkus.http.host=0.0.0.0",
                                "-Dquarkus.http.port=" + applicationPort,
                                "-Djava.util.logging.manager=org.jboss.logmanager.LogManager",
                                "-Xmx128m",
                                "-Ddocker.daemon.tcp.host=localhost",
                                "-Dquarkus.datasource.workers.jdbc.url=" + postgresWorkersContainer.getJdbcUrl(),
                                "-Dquarkus.datasource.workers.username=postgres",
                                "-Dquarkus.datasource.workers.password=postgres")
                                .collect(Collectors.joining(" ")))
                .withNetworkMode("host")
                .waitingFor(Wait.forLogMessage(".*End image initialization.*", 1));
        workerOrchestratorContainer.start();
        System.setProperty("tanksoar.worker_orchestrator.http.port", applicationPort);
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        System.clearProperty("quarkus.datasource.workers.jdbc.url");
        System.clearProperty("quarkus.datasource.workers.username");
        System.clearProperty("quarkus.datasource.workers.password");
        System.clearProperty("tanksoar.worker_orchestrator.http.port");
        if (workerOrchestratorContainer != null) {
            workerOrchestratorContainer.close();
        }
        if (postgresWorkersContainer != null) {
            postgresWorkersContainer.close();
        }
    }
}
