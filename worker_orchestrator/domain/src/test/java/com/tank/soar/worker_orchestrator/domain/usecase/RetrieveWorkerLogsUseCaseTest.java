package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetrieveWorkerLogsUseCaseTest {

    private RetrieveWorkerLogsUseCase retrieveWorkerLogsUseCase;

    private WorkerContainerManager workerContainerManager;
    private WorkerRepository workerRepository;

    @BeforeEach
    public void setup() {
        workerContainerManager = mock(WorkerContainerManager.class);
        workerRepository = mock(WorkerRepository.class);
        retrieveWorkerLogsUseCase = new RetrieveWorkerLogsUseCase(workerContainerManager, workerRepository);
    }

    @Test
    public void should_get_log_from_container_when_exists() throws Exception {
        // Given
        final LogStream logStream = mock(LogStream.class);
        doReturn(Optional.of(Collections.singletonList(logStream))).when(workerContainerManager).findLog(new WorkerId("id"),
                Boolean.TRUE, Boolean.FALSE);

        // When
        final List<? extends LogStream> logStreams = retrieveWorkerLogsUseCase.execute(RetrieveWorkerLogsCommand.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withStdOut(Boolean.TRUE)
                .withStdErr(Boolean.FALSE).build());

        // Then
        assertThat(logStreams).hasSize(1);
        assertThat(logStreams.get(0)).isEqualTo(logStream);
    }

    @Test
    public void should_get_log_from_repository_when_worker_container_is_deleted() throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).findLog(any(), any(), any());
        final LogStream logStream = mock(LogStream.class);
        doReturn(Collections.singletonList(logStream)).when(workerRepository).getLog(new WorkerId("id"), Boolean.TRUE, Boolean.FALSE);

        // When
        final List<? extends LogStream> logStreams = retrieveWorkerLogsUseCase.execute(RetrieveWorkerLogsCommand.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withStdOut(Boolean.TRUE)
                .withStdErr(Boolean.FALSE).build());

        // Then
        assertThat(logStreams).hasSize(1);
        assertThat(logStreams.get(0)).isEqualTo(logStream);
    }

    @Test
    public void should_throw_UnknownWorkerUseCaseException_when_worker_container_is_deleted_and_worker_repository_is_not_present()
            throws Exception {
        // Given
        doReturn(Optional.empty()).when(workerContainerManager).findLog(any(), any(), any());
        doThrow(new UnknownWorkerException(new WorkerId("id"))).when(workerRepository).getLog(any(), any(), any());

        // When && Then
        assertThatThrownBy(() -> retrieveWorkerLogsUseCase.execute(RetrieveWorkerLogsCommand.newBuilder()
                .withWorkerId(new WorkerId("id"))
                .withStdOut(Boolean.TRUE)
                .withStdErr(Boolean.FALSE)
                .build()))
                .isInstanceOf(UnknownWorkerUseCaseException.class)
                .hasFieldOrPropertyWithValue("unknownWorkerId", new WorkerId("id"));
    }

}
