package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerRepository<INFRA extends ContainerInternalData> extends LoggingProvider {

    Worker saveWorker(Worker worker, INFRA containerInternalData, WorkerLog stdOut, WorkerLog stdErr);

    List<? extends Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

}
