package com.tank.soar.worker_orchestrator.domain;

import java.util.List;

public interface WorkerContainerManager<INFRA extends ContainerInternalData> extends LoggingProvider {

    Worker runScript(String script) throws UnableToRunScriptException;

    List<? extends Worker> listAllContainers();

    Worker getContainer(WorkerId workerId) throws UnknownWorkerException;

    INFRA getContainerMetadata(WorkerId workerId) throws UnknownWorkerException;

    void deleteContainer(WorkerId workerId) throws UnknownWorkerException;

}
