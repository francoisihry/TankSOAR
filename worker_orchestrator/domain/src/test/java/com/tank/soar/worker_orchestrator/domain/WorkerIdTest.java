package com.tank.soar.worker_orchestrator.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class WorkerIdTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerId.class).verify();
    }

}
