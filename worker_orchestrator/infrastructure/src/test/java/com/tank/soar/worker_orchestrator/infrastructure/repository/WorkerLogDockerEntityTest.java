package com.tank.soar.worker_orchestrator.infrastructure.repository;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class WorkerLogDockerEntityTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerLogDockerEntity.class).verify();
    }

}
