package com.tank.soar.worker_orchestrator.domain;

public interface WorkerLog {

    WorkerId workerId();

    String log();

    Boolean hasFinishedProducingLog();

}
