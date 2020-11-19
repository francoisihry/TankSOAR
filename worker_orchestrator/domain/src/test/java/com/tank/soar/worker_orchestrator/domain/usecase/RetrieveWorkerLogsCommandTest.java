package com.tank.soar.worker_orchestrator.domain.usecase;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class RetrieveWorkerLogsCommandTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(RetrieveWorkerLogsCommand.class).verify();
    }

}
