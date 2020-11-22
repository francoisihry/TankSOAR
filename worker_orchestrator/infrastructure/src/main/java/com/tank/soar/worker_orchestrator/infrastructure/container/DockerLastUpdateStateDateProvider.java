package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;

public interface DockerLastUpdateStateDateProvider {

    UTCZonedDateTime lastUpdateStateDate(InspectContainerResponse inspectContainerResponse);

}
