package com.tank.soar.worker_orchestrator.domain;

import java.util.List;
import java.util.Optional;

public interface WorkerContainerManager {

    Worker runScript(WorkerId workerId, String script) throws UnableToRunScriptException;

    List<? extends Worker> listAllWorkers();

    Optional<Worker> findWorker(WorkerId workerId);

    void deleteWorker(WorkerId workerId) throws UnknownWorkerException;

    Optional<List<? extends LogStream>> findLog(WorkerId workerId, Boolean stdOut, Boolean stdErr) throws UnknownWorkerException;

}
