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

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        transactionalUseCase = mock(TransactionalUseCase.class);
        runScriptUseCase = new RunScriptUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Test
    public void should_return_worker_container() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(worker).when(workerContainerManager).runScript("script");

        // When
        final Worker workerExecutingScript = runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        assertThat(workerExecutingScript).isEqualTo(worker);
    }

    @Test
    public void should_save_worker_container_state() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(worker).when(workerContainerManager).runScript("script");
        final ContainerInternalData containerInternalData = mock(ContainerInternalData.class);
        doReturn(containerInternalData).when(workerContainerManager).getContainerMetadata(new WorkerId("id"));
        final WorkerLog stdOut = mock(WorkerLog.class);
        doReturn(stdOut).when(workerContainerManager).getStdOut(new WorkerId("id"));
        final WorkerLog stdErr = mock(WorkerLog.class);
        doReturn(stdErr).when(workerContainerManager).getStdErr(new WorkerId("id"));
        final InOrder inOrder = inOrder(workerRepository, transactionalUseCase);

        // When
        runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        inOrder.verify(transactionalUseCase).begin();
        inOrder.verify(workerRepository).saveWorker(worker, containerInternalData, stdOut, stdErr);
        inOrder.verify(transactionalUseCase).commit();
    }

}
