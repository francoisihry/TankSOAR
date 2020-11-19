package com.tank.soar.worker_orchestrator.domain;

import org.apache.commons.lang3.Validate;

public interface LogStream {

    WorkerId workerId();

    LogStreamType logStreamType();

    String content();

    default String toJsonStringRepresentation() {
        Validate.validState(!content().contains("\n"));
        Validate.validState(!content().contains("\r"));
        return String.format("{\"workerId\":\"%s\",\"logStreamType\":\"%s\",\"content\":\"%s\"}",
                workerId().id(),
                logStreamType().name(),
                content());
    }

}
