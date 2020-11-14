package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerRepository {

    Worker createWorker(Worker worker, String script, ContainerInformation containerInformation, WorkerLog stdOut, WorkerLog stdErr);

    Worker saveWorker(Worker worker, ContainerInformation containerInformation, WorkerLog stdOut, WorkerLog stdErr);

    List<? extends Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdOut(WorkerId workerId) throws UnknownWorkerException;

    WorkerLog getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
