package com.tank.soar.worker_orchestrator.infrastructure.container;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class NewDockerContainerWorkerTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(NewDockerContainerWorker.class).verify();
    }

}
