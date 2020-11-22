package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.model.StreamType;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerUtilsTest {

    @ParameterizedTest
    @CsvSource({"STDIN,STDIN", "STDOUT,STDOUT", "STDERR,STDERR", "RAW,RAW"})
    public void should_map_from_docker_streamType_to_domain_LogStreamType(final StreamType givenStreamType,
                                                                          final LogStreamType expectedLogStreamType) {
        // Given

        // When
        final LogStreamType logStreamType = ContainerUtils.mapFromStreamType(givenStreamType);

        // Then
        assertThat(logStreamType).isEqualTo(expectedLogStreamType);
    }

}
