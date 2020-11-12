package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.UnknownWorkerException;
import com.tank.soar.worker_orchestrator.domain.WorkerContainerManager;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

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
        doReturn("stdOut").when(workerContainerManager).getStdOut(new WorkerId("id"));

        // When
        final String stdOut = retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdOut).isEqualTo("stdOut");
    }

    @Test
    public void should_get_stdout_from_repository_when_worker_container_is_deleted() throws Exception {
        // Given
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerContainerManager).getStdOut(new WorkerId("id"));
        doReturn("stdOut").when(workerRepository).getStdOut(new WorkerId("id"));

        // When
        final String stdOut = retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // Then
        assertThat(stdOut).isEqualTo("stdOut");
    }

    @Test
    public void should_get_stdout_from_repository_execute_in_transactional_scope_when_worker_container_is_deleted() throws Exception {
        // Given
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerContainerManager).getStdOut(new WorkerId("id"));
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
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerContainerManager).getStdOut(new WorkerId("id"));
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
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerContainerManager).getStdOut(new WorkerId("id"));
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
