package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetWorkerUseCaseTest {

    private GetWorkerUseCase getWorkerUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        getWorkerUseCase = new GetWorkerUseCase(workerContainerManager, workerRepository);
    }

    @Test
    public void should_find_worker_from_container_when_exists() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(Optional.of(worker)).when(workerContainerManager).findWorker(new WorkerId("id"));

        // When
        final Worker workerGet = getWorkerUseCase.execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(workerGet).isEqualTo(worker);
    }

    @Test
    public void should_find_worker_from_repository_when_worker_container_is_deleted() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(Optional.empty()).when(workerContainerManager).findWorker(new WorkerId("id"));
        doReturn(worker).when(workerRepository).getWorker(new WorkerId("id"));

        // When
        final Worker workerGet = getWorkerUseCase.execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(workerGet).isEqualTo(worker);
    }

    @Test
    public void should_throw_UnknownWorkerUseCaseException_when_worker_container_is_deleted_and_worker_repository_is_not_present()
            throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).findWorker(new WorkerId("id"));
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).getWorker(new WorkerId("id"));

        // When && Then
        assertThatThrownBy(() -> getWorkerUseCase.execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId("id")).build()))
                .isInstanceOf(UnknownWorkerUseCaseException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

}
