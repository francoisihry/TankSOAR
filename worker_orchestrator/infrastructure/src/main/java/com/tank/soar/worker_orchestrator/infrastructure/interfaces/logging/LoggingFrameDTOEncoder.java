package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class LoggingFrameDTOEncoder implements Encoder.Text<LoggingFrameDTO> {

    @Override
    public String encode(final LoggingFrameDTO object) throws EncodeException {
        return object.toJson().toString();
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
