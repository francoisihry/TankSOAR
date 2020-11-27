package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RunScriptUseCaseTest {

    private RunScriptUseCase runScriptUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;
    private WorkerIdProvider workerIdProvider;
    private UTCZonedDateTimeProvider utcZonedDateTimeProvider;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        workerIdProvider = mock(WorkerIdProvider.class);
        utcZonedDateTimeProvider = mock(UTCZonedDateTimeProvider.class);
        doReturn(new WorkerId("id")).when(workerIdProvider).provideNewWorkerId();
        runScriptUseCase = new RunScriptUseCase(workerContainerManager, workerRepository,
                workerIdProvider, utcZonedDateTimeProvider);
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
    public void should_save_created_worker() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(worker).when(workerContainerManager).runScript(new WorkerId("id"),"script");
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00))
                .when(utcZonedDateTimeProvider).now();

        // When
        runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        verify(workerRepository, times(1)).createWorker(new WorkerId("id"),"script",
                UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00));
    }

    @Test
    public void should_save_created_worker_before_running_script() throws Exception {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(worker).when(workerContainerManager).runScript(new WorkerId("id"),"script");
        final InOrder inOrder = inOrder(workerRepository, workerContainerManager);

        // When
        runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript("script").build());

        // Then
        // inOrder.verify(workerLockMechanism, times(1)).lock(new WorkerId("id"));
        inOrder.verify(workerRepository, times(1)).createWorker(any(), any(), any());
        inOrder.verify(workerContainerManager, times(1)).runScript(any(), any());
    }

}
