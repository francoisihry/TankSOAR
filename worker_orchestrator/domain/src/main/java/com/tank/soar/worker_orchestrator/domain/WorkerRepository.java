package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerRepository {

    WorkerId createWorker(WorkerId workerId, String script, UTCZonedDateTime createdAt, UTCZonedDateTime lastUpdateStateDate);

    Worker saveWorker(Worker worker, ContainerInformation containerInformation, WorkerLog stdOut, WorkerLog stdErr);

    Boolean hasWorker(WorkerId workerId);

    List<? extends Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdOut(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
