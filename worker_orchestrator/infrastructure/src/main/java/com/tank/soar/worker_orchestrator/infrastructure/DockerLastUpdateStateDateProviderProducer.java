package com.tank.soar.worker_orchestrator.infrastructure;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTimeProvider;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerLastUpdateStateDateProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class DockerLastUpdateStateDateProviderProducer {

    @Inject
    UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    @ApplicationScoped
    @Produces
    public DockerLastUpdateStateDateProvider dockerCreatedAtProviderProducer() {
        return new DockerLastUpdateStateDateProvider() {

            @Override
            public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse) {
                return DockerContainerStatus.fromDockerStatus(inspectContainerResponse.getState().getStatus())
                        .lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);
            }

        };
    }

}
