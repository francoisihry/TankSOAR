package com.tank.soar.worker_orchestrator.infrastructure;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class NewWorkerEventTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(NewWorkerEvent.class).verify();
    }

}
