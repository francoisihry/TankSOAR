package com.tank.soar.worker_orchestrator.domain;

import java.util.List;
import java.util.Optional;

public interface WorkerContainerManager {

    Worker runScript(String script) throws UnableToRunScriptException;

    List<? extends Worker> listAllContainers();

    Optional<Worker> findContainer(WorkerId workerId);

    ContainerInformation getContainerMetadata(WorkerId workerId) throws UnknownWorkerException;

    void deleteContainer(WorkerId workerId) throws UnknownWorkerException;

    Optional<WorkerLog> getStdOut(WorkerId workerId);

    Optional<WorkerLog> getStdErr(WorkerId workerId);

}
