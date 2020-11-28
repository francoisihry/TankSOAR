package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.infrastructure.NewWorkerEvent;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class NewWorkerEventTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(NewWorkerEvent.class).verify();
    }

}
