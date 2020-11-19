package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class LogStreamDTOTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(LogStreamDTO.class).verify();
    }

}
