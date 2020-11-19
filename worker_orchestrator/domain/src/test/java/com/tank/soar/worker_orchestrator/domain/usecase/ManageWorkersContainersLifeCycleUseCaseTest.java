package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageWorkersContainersLifeCycleUseCaseTest {

    private ManageWorkersContainersLifeCycleUseCase manageWorkersContainersLifeCycleUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;

    @BeforeEach
    public void setup() throws Exception {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        manageWorkersContainersLifeCycleUseCase = new ManageWorkersContainersLifeCycleUseCase(workerContainerManager, workerRepository);
        doReturn(Optional.of(Collections.emptyList())).when(workerContainerManager).findLog(new WorkerId("id"), Boolean.TRUE, Boolean.TRUE);
        doReturn(true).when(workerRepository).hasWorker(new WorkerId("id"));
    }

    @Test
    public void should_save_container_worker() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(Collections.singletonList(worker)).when(workerContainerManager).listAllContainers();
        final ContainerInformation containerInformation = mock(ContainerInformation.class);
        doReturn(containerInformation).when(workerContainerManager).getContainerMetadata(new WorkerId("id"));
        final InOrder inOrder = inOrder(workerRepository);

        // When
        manageWorkersContainersLifeCycleUseCase.execute(new VoidCommand());

        // Then
        inOrder.verify(workerRepository).hasWorker(new WorkerId("id"));
        inOrder.verify(workerRepository).saveWorker(worker, containerInformation, Collections.emptyList());
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
        verify(workerContainerManager, times(0)).deleteContainer(any());
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
        verify(workerContainerManager, times(1)).deleteContainer(new WorkerId("id"));
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
        verify(workerContainerManager, times(1)).deleteContainer(new WorkerId("id"));
    }

}
