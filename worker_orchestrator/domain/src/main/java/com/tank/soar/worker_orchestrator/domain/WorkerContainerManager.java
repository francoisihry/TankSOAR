package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerContainerManager<INFRA extends ContainerMetadata> {

    Worker runScript(String script) throws UnableToRunScriptException;

    List<Worker> listAllContainers();

    Worker getContainer(WorkerId workerId) throws UnknownWorkerException;

    INFRA getContainerMetadata(WorkerId workerId) throws UnknownWorkerException;

    void removeContainer(WorkerId workerId) throws UnknownWorkerException;

    String getStdOut(WorkerId workerId) throws UnknownWorkerException;

    String getStdErr(WorkerId workerId) throws UnknownWorkerException;

}
