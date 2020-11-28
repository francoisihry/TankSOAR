package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerRepository {

    WorkerId createWorker(WorkerId workerId, String script, UTCZonedDateTime createdAt);

    List<? extends Worker> listAllWorkers();

    Worker getWorker(WorkerId workerId) throws UnknownWorkerException;

    List<? extends LogStream> getLog(WorkerId workerId, Boolean stdOut, Boolean stdErr) throws UnknownWorkerException;

    Worker markWorkerAsManuallyStopped(WorkerId workerId, UTCZonedDateTime markManuallyStoppedAt) throws UnknownWorkerException;

}
