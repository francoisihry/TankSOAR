package com.tank.soar.worker_orchestrator.domain.usecase;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class GetWorkerCommandTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(GetWorkerCommand.class).verify();
    }

}
