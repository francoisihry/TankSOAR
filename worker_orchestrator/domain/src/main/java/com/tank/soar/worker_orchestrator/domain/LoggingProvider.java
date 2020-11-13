package com.tank.soar.worker_orchestrator.domain;

public interface LoggingProvider {

    WorkerLog getStdOut(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
