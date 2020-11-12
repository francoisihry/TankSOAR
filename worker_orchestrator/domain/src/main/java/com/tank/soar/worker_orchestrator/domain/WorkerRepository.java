package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerRepository<INFRA extends ContainerMetadata> {

    Worker saveWorker(Worker worker, INFRA containerMetadata);

    List<Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

    String getStdOut(WorkerId workerId) throws UnknownWorkerException;

    String getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
