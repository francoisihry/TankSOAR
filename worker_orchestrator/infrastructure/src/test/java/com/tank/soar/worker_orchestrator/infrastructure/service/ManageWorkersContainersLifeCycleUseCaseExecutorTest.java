package com.tank.soar.worker_orchestrator.infrastructure.service;

import com.tank.soar.worker_orchestrator.domain.usecase.ManageWorkersContainersLifeCycleUseCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ManageWorkersContainersLifeCycleUseCaseExecutorTest {

    @InjectMock
    ManageWorkersContainersLifeCycleUseCase manageWorkersContainersLifeCycleUseCase;

    @Test
    public void should_schedule_run() throws Exception {
        // Given

        // When
        TimeUnit.SECONDS.sleep(25);

        // Then
        verify(manageWorkersContainersLifeCycleUseCase, atLeast(2)).execute(any());
    }

}
