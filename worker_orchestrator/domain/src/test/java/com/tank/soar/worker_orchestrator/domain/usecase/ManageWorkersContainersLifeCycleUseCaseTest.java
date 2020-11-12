package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageWorkersContainersLifeCycleUseCaseTest {

    private ManageWorkersContainersLifeCycleUseCase manageWorkersContainersLifeCycleUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private TransactionalUseCase transactionalUseCase;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        transactionalUseCase = mock(TransactionalUseCase.class);
        manageWorkersContainersLifeCycleUseCase = new ManageWorkersContainersLifeCycleUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Test
    public void should_save_container_worker() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(Collections.singletonList(worker)).when(workerContainerManager).listAllContainers();
        final ContainerMetadata containerMetadata = mock(ContainerMetadata.class);
        doReturn(containerMetadata).when(workerContainerManager).getContainerMetadata(new WorkerId("id"));
        final InOrder inOrder = inOrder(workerRepository, transactionalUseCase);

        // When
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());

        // Then
        inOrder.verify(transactionalUseCase).begin();
        inOrder.verify(workerRepository).saveWorker(worker, containerMetadata);
        inOrder.verify(transactionalUseCase).commit();
    }

    @Test
    public void should_not_remove_container_when_container_is_running() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.RUNNING).when(worker).workerStatus();
        when(worker.hasFinished()).thenCallRealMethod();
        doReturn(Collections.singletonList(worker)).when(workerContainerManager).listAllContainers();

        // When
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());

        // Then
        verify(workerContainerManager, times(0)).removeContainer(any());
    }

    @Test
    public void should_remove_container_when_container_has_finished() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        when(worker.hasFinished()).thenCallRealMethod();
        doReturn(Collections.singletonList(worker)).when(workerContainerManager).listAllContainers();

        // When
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());

        // Then
        verify(workerContainerManager, times(1)).removeContainer(new WorkerId("id"));
    }

    @Test
    public void should_remove_container_when_container_is_in_error() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.ERROR).when(worker).workerStatus();
        when(worker.hasFinished()).thenCallRealMethod();
        doReturn(Collections.singletonList(worker)).when(workerContainerManager).listAllContainers();

        // When
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());

        // Then
        verify(workerContainerManager, times(1)).removeContainer(new WorkerId("id"));
    }

}
