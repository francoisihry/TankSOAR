package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;

public interface UTCZonedDateTimeProvider {

    UTCZonedDateTime now();

}
