package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RunScriptUseCaseTest {

    private RunScriptUseCase runScriptUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private TransactionalUseCase transactionalUseCase;
    private WorkerIdProvider workerIdProvider;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        transactionalUseCase = mock(TransactionalUseCase.class);
        workerIdProvider = mock(WorkerIdProvider.class);
        doReturn(new WorkerId("id")).when(workerIdProvider).provideNewWorkerId();
        runScriptUseCase = new RunScriptUseCase(workerContainerManager, workerRepository, transactionalUseCase, workerIdProvider);
    }

    @Test
    public void should_return_worker_container() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(worker).when(workerContainerManager).runScript(new WorkerId("id"), "script");

        // When
        final Worker workerExecutingScript = runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        assertThat(workerExecutingScript).isEqualTo(worker);
    }

    @Test
    public void should_save_worker_container_state() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(worker).when(workerContainerManager).runScript(new WorkerId("id"),"script");
        final InOrder inOrder = inOrder(workerRepository, transactionalUseCase);

        // When
        runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        inOrder.verify(transactionalUseCase).begin();
        inOrder.verify(workerRepository).createWorker(eq(new WorkerId("id")), eq("script"), any(), any());
        inOrder.verify(transactionalUseCase).commit();
    }

}
