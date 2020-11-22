package com.tank.soar.worker_orchestrator.infrastructure.interfaces.workersLifecycle;

import com.tank.soar.worker_orchestrator.infrastructure.interfaces.WorkerDTO;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class WorkerDTOEncoder implements Encoder.Text<WorkerDTO> {

    @Override
    public String encode(final WorkerDTO workerDTO) throws EncodeException {
        return workerDTO.toJsonStringRepresentation();
    }

    @Override
    public void init(final EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
