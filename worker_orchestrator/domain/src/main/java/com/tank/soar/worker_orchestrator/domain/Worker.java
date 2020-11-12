package com.tank.soar.worker_orchestrator.domain;

import java.time.LocalDateTime;
import java.util.Arrays;

public interface Worker {

    WorkerId workerId();

    WorkerStatus workerStatus();

    Source source();

    LocalDateTime lastUpdateStateDate();

    default boolean hasFinished() {
        return Arrays.asList(WorkerStatus.FINISHED, WorkerStatus.ERROR)
                .contains(workerStatus());
    }

}
