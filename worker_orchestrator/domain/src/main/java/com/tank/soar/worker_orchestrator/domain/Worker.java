package com.tank.soar.worker_orchestrator.domain;

import java.util.Arrays;

public interface Worker {

    WorkerId workerId();

    WorkerStatus workerStatus();

    Source source();

    UTCZonedDateTime lastUpdateStateDate();

    UTCZonedDateTime createdAt();

    default boolean hasFinished() {
        // TODO je devrais rajouter un test parametrée ...
        return Arrays.asList(WorkerStatus.ERROR_CREATING, WorkerStatus.FINISHED, WorkerStatus.ERROR)
                .contains(workerStatus());
    }

}
