package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class WorkerDockerEntityTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerDockerEntity.class).verify();
    }

    @Test
    public void should_fail_fast_when_no_worker_event_is_present() {
        // Given

        // When && Then
        assertThatThrownBy(() -> WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withWorkerEventEntities(Collections.emptyList())
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    private List<WorkerEventEntity> workerEventEntities;

    @BeforeEach
    public void setup() {
        workerEventEntities = Arrays.asList(
                new WorkerEventEntity(new WorkerId("id"), EventType.USER, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00), null, UserEventType.CREATION_REQUESTED),
                new WorkerEventEntity(new WorkerId("id"), EventType.DOCKER, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 02, 00), DockerContainerStatus.CREATED, null),
                new WorkerEventEntity(new WorkerId("id"), EventType.DOCKER, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 03, 00), DockerContainerStatus.RUNNING, null),
                new WorkerEventEntity(new WorkerId("id"), EventType.DOCKER, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 04, 00), DockerContainerStatus.EXITED, null)
        );
    }

    @Test
    public void should_worker_status_be_defined_to_the_last_snapshot_worker_status() {
        // Given

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withWorkerEventEntities(workerEventEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.workerStatus()).isEqualTo(WorkerStatus.FINISHED);
    }

    @Test
    public void should_last_update_date_be_defined_to_the_last_snapshot_worker_status() {
        // Given

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withWorkerEventEntities(workerEventEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.lastUpdateStateDate()).isEqualTo(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 04, 00));
    }

}
