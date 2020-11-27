package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerStatus;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTimeProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public enum DockerContainerStatus {

    CREATED {
        @Override
        public String dockerStatus() {
            return "created";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.CREATED;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return UTCZonedDateTime.of(ZonedDateTime.parse(inspectContainerResponse.getCreated())
                    .withZoneSameInstant(ZoneOffset.UTC));
        }

    },
    RESTARTING {
        @Override
        public String dockerStatus() {
            return "restarting";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.RUNNING;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return UTCZonedDateTime.of(ZonedDateTime.parse(inspectContainerResponse.getState().getStartedAt())
                    .withZoneSameInstant(ZoneOffset.UTC));
        }

    },
    RUNNING {
        @Override
        public String dockerStatus() {
            return "running";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.RUNNING;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return utcZonedDateTimeProvider.now();
        }

    },
    REMOVING {
        @Override
        public String dockerStatus() {
            return "removing";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.FINISHED;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return utcZonedDateTimeProvider.now();
        }

    },
    PAUSED {
        @Override
        public String dockerStatus() {
            return "paused";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.RUNNING;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return utcZonedDateTimeProvider.now();
        }

    },
    EXITED {
        @Override
        public String dockerStatus() {
            return "exited";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.FINISHED;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return UTCZonedDateTime.of(ZonedDateTime.parse(inspectContainerResponse.getState().getFinishedAt())
                    .withZoneSameInstant(ZoneOffset.UTC));
        }

    },
    DEAD {
        @Override
        public String dockerStatus() {
            return "dead";
        }

        @Override
        public WorkerStatus toWorkerStatus() {
            return WorkerStatus.ERROR;
        }

        @Override
        public UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                    final UTCZonedDateTimeProvider utcZonedDateTimeProvider) {
            return UTCZonedDateTime.of(ZonedDateTime.parse(inspectContainerResponse.getState().getFinishedAt())
                    .withZoneSameInstant(ZoneOffset.UTC));
        }

    }
    ;

    public abstract String dockerStatus();

    public abstract WorkerStatus toWorkerStatus();

    public abstract UTCZonedDateTime lastUpdateStateDate(final InspectContainerResponse inspectContainerResponse,
                                                         final UTCZonedDateTimeProvider utcZonedDateTimeProvider);

    public static final DockerContainerStatus fromDockerStatus(final String dockerStatus) {
        return Arrays.asList(DockerContainerStatus.values())
                .stream()
                .filter(dockerContainerStatusValue -> dockerStatus.equals(dockerContainerStatusValue.dockerStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported docker status '%s'", dockerStatus)));
    }

}
