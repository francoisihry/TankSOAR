package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerContainerStatusTest {

    @ParameterizedTest
    @CsvSource({"created,CREATED", "restarting,RUNNING", "running,RUNNING",
                "removing,FINISHED", "paused,RUNNING", "exited,FINISHED", "dead,ERROR"})
    public void should_map_from_docker_status_to_worker_status(final String givenDockerStatus,
                                                               final WorkerStatus expectedWorkerStatus) {
        // Given

        // When
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.fromDockerStatus(givenDockerStatus);

        // Then
        assertThat(dockerContainerStatus.toWorkerStatus()).isEqualTo(expectedWorkerStatus);
    }

}
