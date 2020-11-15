package com.tank.soar.worker_orchestrator.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkerRepository {

    WorkerId createWorker(WorkerId workerId, String script, LocalDateTime createdAt, LocalDateTime lastUpdateStateDate);

    Worker saveWorker(Worker worker, ContainerInformation containerInformation, WorkerLog stdOut, WorkerLog stdErr);

    List<? extends Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdOut(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
