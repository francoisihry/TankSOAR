package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class UTCZonedDateTimeProviderProducer {

    @Produces
    @ApplicationScoped
    public UTCZonedDateTimeProvider utcZonedDateTimeProviderProducer() {
        return () -> UTCZonedDateTime.now();
    }

}
