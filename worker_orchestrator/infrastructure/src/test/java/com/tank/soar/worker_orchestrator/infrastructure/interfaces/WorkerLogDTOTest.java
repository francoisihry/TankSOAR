package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class WorkerLogDTOTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerLogDTO.class).verify();
    }

}
