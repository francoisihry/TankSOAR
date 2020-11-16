package com.tank.soar.worker_orchestrator.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.Map;

public class PostgresqlTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> postgresWorkersContainer;

    @Override
    public Map<String, String> start() {
        postgresWorkersContainer = new PostgreSQLContainer<>("postgres:11-alpine")
                .withDatabaseName("worker")
                .withUsername("postgres")
                .withPassword("postgres");
        postgresWorkersContainer.start();
        System.setProperty("quarkus.datasource.workers.jdbc.url", postgresWorkersContainer.getJdbcUrl());
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        System.clearProperty("quarkus.datasource.workers.jdbc.url");
        if (postgresWorkersContainer != null) {
            postgresWorkersContainer.close();
        }
    }

}
