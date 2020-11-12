package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.domain.WorkerContainerManager;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListWorkersUseCaseTest {

    private ListWorkersUseCase listWorkersUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private TransactionalUseCase transactionalUseCase;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        transactionalUseCase = mock(TransactionalUseCase.class);
        listWorkersUseCase = new ListWorkersUseCase(workerContainerManager, workerRepository, transactionalUseCase);
    }

    @Test
    public void should_list_workers_return_following_last_update_state_date() throws Exception {
        // Given
        final Worker worker1 = mock(Worker.class);
        doReturn(new WorkerId("id1")).when(worker1).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00)).when(worker1).lastUpdateStateDate();
        final Worker workerContainer1 = mock(Worker.class);
        doReturn(new WorkerId("id1")).when(workerContainer1).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(workerContainer1).lastUpdateStateDate();
        final Worker worker2 = mock(Worker.class);
        doReturn(new WorkerId("id2")).when(worker2).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker2).lastUpdateStateDate();
        final Worker workerContainer2 = mock(Worker.class);
        doReturn(new WorkerId("id2")).when(workerContainer2).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 20, 00)).when(workerContainer2).lastUpdateStateDate();

        doReturn(Arrays.asList(workerContainer1, workerContainer2)).when(workerContainerManager).listAllContainers();
        doReturn(Arrays.asList(worker1, worker2)).when(workerRepository).listAllWorkers();

        // When
        final List<Worker> workers = listWorkersUseCase.execute(new ListWorkersCommand());

        // Then
        assertThat(workers).containsExactly(workerContainer1, workerContainer2);
    }

    @Test
    public void should_list_workers_return_worker_in_repository_when_no_more_present_in_container() throws Exception {
        // Given
        final Worker worker1 = mock(Worker.class);
        doReturn(new WorkerId("id1")).when(worker1).workerId();
        final Worker worker2 = mock(Worker.class);
        doReturn(new WorkerId("id2")).when(worker2).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker2).lastUpdateStateDate();
        final Worker workerContainer2 = mock(Worker.class);
        doReturn(new WorkerId("id2")).when(workerContainer2).workerId();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 20, 00)).when(workerContainer2).lastUpdateStateDate();

        doReturn(Arrays.asList(workerContainer2)).when(workerContainerManager).listAllContainers();
        doReturn(Arrays.asList(worker1, worker2)).when(workerRepository).listAllWorkers();

        // When
        final List<Worker> workers = listWorkersUseCase.execute(new ListWorkersCommand());

        // Then
        assertThat(workers).containsExactly(worker1, workerContainer2);
    }

}
