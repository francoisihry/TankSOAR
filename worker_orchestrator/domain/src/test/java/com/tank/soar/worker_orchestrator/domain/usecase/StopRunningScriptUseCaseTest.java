package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StopRunningScriptUseCaseTest {

    private StopRunningScriptUseCase stopRunningScriptUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        utcZonedDateTimeProvider = mock(UTCZonedDateTimeProvider.class);
        stopRunningScriptUseCase = new StopRunningScriptUseCase(workerContainerManager, workerRepository,
                utcZonedDateTimeProvider);
    }

    @Test
    public void should_delete_container_worker() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(Optional.of(worker)).when(workerContainerManager).findWorker(new WorkerId("id"));
        doReturn(mock(Worker.class)).when(workerRepository).markWorkerAsManuallyStopped(any(), any());

        // When
        stopRunningScriptUseCase.execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        verify(workerContainerManager, times(1)).deleteWorker(new WorkerId("id"));
    }

    @Test
    public void should_mark_worker_as_manually_stopped() throws Exception {
        // Given
        final Worker workerContainer = mock(Worker.class);
        doReturn(Optional.of(workerContainer)).when(workerContainerManager).findWorker(new WorkerId("id"));
        final Worker worker = mock(Worker.class);
        final UTCZonedDateTime utcZonedDateTime = UTCZonedDateTime.now();
        doReturn(utcZonedDateTime).when(utcZonedDateTimeProvider).now();
        doReturn(worker).when(workerRepository).markWorkerAsManuallyStopped(new WorkerId("id"), utcZonedDateTime);

        // When
        final Worker workerStopped = stopRunningScriptUseCase.execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(workerStopped).isEqualTo(worker);
    }

    @Test
    public void should_throw_unknown_worker_use_case_exception_when() throws Exception {
        // Given
        final Worker workerContainer = mock(Worker.class);
        doReturn(Optional.of(workerContainer)).when(workerContainerManager).findWorker(new WorkerId("id"));
        final UTCZonedDateTime utcZonedDateTime = UTCZonedDateTime.now();
        doReturn(utcZonedDateTime).when(utcZonedDateTimeProvider).now();
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).markWorkerAsManuallyStopped(new WorkerId("id"), utcZonedDateTime);

        // When && Then
        assertThatThrownBy(() -> stopRunningScriptUseCase.execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build()))
                .isInstanceOf(UnknownWorkerUseCaseException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    public void should_throw_worker_already_deleted_use_case_exception_when_worker_container_already_deleted() {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).findWorker(new WorkerId("id"));

        // When && Then
        assertThatThrownBy(() -> stopRunningScriptUseCase.execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build()))
                .isInstanceOf(WorkerAlreadyDeletedUseCaseException.class)
                .hasFieldOrPropertyWithValue("workerId", new WorkerId("id"));
    }

}
