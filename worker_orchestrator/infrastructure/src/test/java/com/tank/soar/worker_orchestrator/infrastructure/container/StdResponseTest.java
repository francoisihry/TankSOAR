package com.tank.soar.worker_orchestrator.infrastructure.container;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class StdResponseTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(StdResponse.class).verify();
    }

}
