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
public class RetrieveWorkerStdErrUseCaseTest {

    private RetrieveWorkerStdErrUseCase retrieveWorkerStdErrUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        retrieveWorkerStdErrUseCase = new RetrieveWorkerStdErrUseCase(workerContainerManager, workerRepository);
    }

    @Test
    public void should_get_stderr_from_container_when_exists() throws Exception {
        // Given
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(Optional.of(workerLog)).when(workerContainerManager).getStdErr(new WorkerId("id"));

        // When
        final WorkerLog stdErr = retrieveWorkerStdErrUseCase.execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdErr).isEqualTo(workerLog);
    }

    @Test
    public void should_get_stderr_from_repository_when_worker_container_is_deleted() throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdErr(new WorkerId("id"));
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(workerLog).when(workerRepository).getStdErr(new WorkerId("id"));

        // When
        final WorkerLog stdErr = retrieveWorkerStdErrUseCase.execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdErr).isEqualTo(workerLog);
    }

    @Test
    public void should_throw_UnknownWorkerUseCaseException_when_worker_container_is_deleted_and_worker_repository_is_not_present()
            throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdErr(new WorkerId("id"));
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).getStdErr(new WorkerId("id"));

        // When && Then
        assertThatThrownBy(() -> retrieveWorkerStdErrUseCase.execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId("id")).build()))
                .isInstanceOf(UnknownWorkerUseCaseException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

}
