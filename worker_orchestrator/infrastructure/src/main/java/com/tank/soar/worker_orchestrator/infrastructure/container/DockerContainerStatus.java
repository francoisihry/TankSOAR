package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.WorkerStatus;

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
    }
    ;

    public abstract String dockerStatus();

    public abstract WorkerStatus toWorkerStatus();

    public static final DockerContainerStatus fromDockerStatus(final String dockerStatus) {
        return Arrays.asList(DockerContainerStatus.values())
                .stream()
                .filter(dockerContainerStatusValue -> dockerStatus.equals(dockerContainerStatusValue.dockerStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported docker status '%s'", dockerStatus)));
    }

}
