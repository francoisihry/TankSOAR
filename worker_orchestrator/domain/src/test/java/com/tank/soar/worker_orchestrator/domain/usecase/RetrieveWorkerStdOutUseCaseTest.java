package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class RetrieveWorkerStdOutUseCaseTest {

    private RetrieveWorkerStdOutUseCase retrieveWorkerStdOutUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private TransactionalUseCase transactionalUseCase;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        transactionalUseCase = mock(TransactionalUseCase.class);
        retrieveWorkerStdOutUseCase = new RetrieveWorkerStdOutUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Test
    public void should_get_stdout_from_container_when_exists() throws Exception {
        // Given
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(Optional.of(workerLog)).when(workerContainerManager).getStdOut(new WorkerId("id"));

        // When
        final WorkerLog stdOut = retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdOut).isEqualTo(workerLog);
    }

    @Test
    public void should_get_stdout_from_repository_when_worker_container_is_deleted() throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdOut(new WorkerId("id"));
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(workerLog).when(workerRepository).getStdOut(new WorkerId("id"));

        // When
        final WorkerLog stdOut = retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdOut).isEqualTo(workerLog);
    }

    @Test
    public void should_get_stdout_from_repository_execute_in_transactional_scope_when_worker_container_is_deleted() throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdOut(new WorkerId("id"));
        final InOrder inOrder = inOrder(workerRepository, transactionalUseCase);

        // When
        retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        inOrder.verify(transactionalUseCase).begin();
        inOrder.verify(workerRepository).getStdOut(new WorkerId("id"));
        inOrder.verify(transactionalUseCase).commit();
    }

    @Test
    public void should_throw_UnknownWorkerUseCaseException_when_worker_container_is_deleted_and_worker_repository_is_not_present()
            throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdOut(new WorkerId("id"));
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).getStdOut(new WorkerId("id"));

        // When && Then
        assertThatThrownBy(() -> retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build()))
                .isInstanceOf(UnknownWorkerUseCaseException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

    @Test
    public void should_rollback_transaction_when_worker_container_is_deleted_and_worker_repository_is_not_present()
            throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).getStdOut(new WorkerId("id"));
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).getStdOut(new WorkerId("id"));

        // When
        try {
            retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());
            fail("should fail");
        } catch (final Exception e) {

        }

        // Then
        verify(transactionalUseCase, times(1)).rollback();
    }

}
