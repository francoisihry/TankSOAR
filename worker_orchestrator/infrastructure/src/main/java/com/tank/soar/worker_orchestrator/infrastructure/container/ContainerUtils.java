package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.model.StreamType;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;

public final class ContainerUtils {

    private ContainerUtils() {}

    // Add to use an helper, utils class :(
    // I cannot use one of the Object to do the mapping.
    public static final LogStreamType mapFromStreamType(final StreamType streamType) {
        switch (streamType) {
            case RAW:
                return LogStreamType.RAW;
            case STDIN:
                return LogStreamType.STDIN;
            case STDERR:
                return LogStreamType.STDERR;
            case STDOUT:
                return LogStreamType.STDOUT;
            default:
                throw new IllegalStateException();
        }
    }

}
