package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.UTCZonedDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private InspectContainerResponse inspectContainerResponse;
    private UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    @BeforeEach
    public void setup() {
        inspectContainerResponse = mock(InspectContainerResponse.class, RETURNS_DEEP_STUBS);
        utcZonedDateTimeProvider = mock(UTCZonedDateTimeProvider.class);
    }

    @Test
    public void should_use_inspected_docker_container_when_docker_container_is_in_created_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.CREATED;
        doReturn("2020-11-22T15:16:30.000000000Z").when(inspectContainerResponse).getCreated();

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_use_inspected_docker_container_when_docker_container_is_in_restarting_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.RESTARTING;
        when(inspectContainerResponse.getState().getStartedAt()).thenReturn("2020-11-22T15:16:30.000000000Z");

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_return_now_time_when_docker_container_is_in_running_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.RUNNING;
        doReturn(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30)).when(utcZonedDateTimeProvider).now();

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_return_now_time_when_docker_container_is_in_removing_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.REMOVING;
        doReturn(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30)).when(utcZonedDateTimeProvider).now();

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_return_now_time_when_docker_container_is_in_paused_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.PAUSED;
        doReturn(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30)).when(utcZonedDateTimeProvider).now();

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_use_inspected_docker_container_when_docker_container_is_in_exited_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.EXITED;
        when(inspectContainerResponse.getState().getFinishedAt()).thenReturn("2020-11-22T15:16:30.000000000Z");

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

    @Test
    public void should_use_inspected_docker_container_when_docker_container_is_in_dead_state() {
        // Given
        final DockerContainerStatus dockerContainerStatus = DockerContainerStatus.DEAD;
        when(inspectContainerResponse.getState().getFinishedAt()).thenReturn("2020-11-22T15:16:30.000000000Z");

        // When
        final UTCZonedDateTime lastUpdateStateDate = dockerContainerStatus.lastUpdateStateDate(inspectContainerResponse, utcZonedDateTimeProvider);

        // Then
        assertThat(lastUpdateStateDate)
                .isEqualTo(UTCZonedDateTime.of(2020, Month.NOVEMBER, 22, 15, 16, 30));
    }

}
