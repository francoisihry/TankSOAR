package com.tank.soar.worker_orchestrator.infrastructure.container;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class WorkerStateChangedTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerStateChanged.class).verify();
    }

}
