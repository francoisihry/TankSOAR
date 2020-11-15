package com.tank.soar.worker_orchestrator.infrastructure;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class DockerCompatibleUUIDNameWorkerIdProviderTest {

    @Inject
    DockerCompatibleUUIDNameWorkerIdProvider dockerCompatibleUuidNameWorkerIdProvider;

    @Test
    public void should_return_an_uuid_compatible_with_docker_naming() {
        // - should not be supported by docker
        // Given

        // When
        final WorkerId workerId = dockerCompatibleUuidNameWorkerIdProvider.provideNewWorkerId();

        // Then
        assertThat(workerId.id()).matches("[0-9a-f]{8}_[0-9a-f]{4}_[0-9a-f]{4}_[0-9a-f]{4}_[0-9a-f]{12}");
    }

}
