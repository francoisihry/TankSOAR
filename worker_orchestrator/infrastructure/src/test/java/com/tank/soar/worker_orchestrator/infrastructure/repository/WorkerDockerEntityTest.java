package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.infrastructure.container.DockerContainerStatus;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class WorkerDockerEntityTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(WorkerDockerEntity.class).verify();
    }

    @Test
    public void should_worker_status_be_defined_to_creating_when_no_snapshot_is_present() {
        // Given
        final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = Collections.emptyList();

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(workerSnapshotDockerEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.workerStatus()).isEqualTo(WorkerStatus.CREATING);
    }

    @Test
    public void should_last_update_date_be_defined_to_created_at_worker_when_no_snapshot_is_present() {
        // Given
        final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = Collections.emptyList();

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(workerSnapshotDockerEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.lastUpdateStateDate()).isEqualTo(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));
    }

    @Test
    public void should_worker_status_be_defined_to_the_last_snapshot_worker_status() {
        // Given
        final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = Arrays.asList(
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.CREATED, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00)),
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.EXITED, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 03, 00)),
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.RUNNING, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 02, 00))
        );

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(workerSnapshotDockerEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.workerStatus()).isEqualTo(WorkerStatus.FINISHED);
    }

    @Test
    public void should_last_update_date_be_defined_to_the_last_snapshot_worker_status() {
        // Given
        final List<DockerStateSnapshotEntity> workerSnapshotDockerEntities = Arrays.asList(
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.CREATED, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 01, 00)),
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.EXITED, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 03, 00)),
                new DockerStateSnapshotEntity(new WorkerId("id"), DockerContainerStatus.RUNNING, UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 02, 00))
        );

        // When
        final WorkerDockerEntity workerDockerEntity = WorkerDockerEntity.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withCreatedAt(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .withWorkerSnapshotDockerEntities(workerSnapshotDockerEntities)
                .build();

        // Then
        assertThat(workerDockerEntity.lastUpdateStateDate()).isEqualTo(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 03, 00));
    }

}
