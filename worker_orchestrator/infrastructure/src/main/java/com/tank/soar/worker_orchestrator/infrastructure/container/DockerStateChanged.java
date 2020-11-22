package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.tank.soar.worker_orchestrator.domain.UTCZonedDateTime;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.List;
import java.util.Objects;

public final class DockerStateChanged {

    private final WorkerId workerId;
    private final InspectContainerResponse container;
    private final List<StdResponse> stdResponses;
    private final UTCZonedDateTime dockerStateChangedDate;

    private DockerStateChanged(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.container = Objects.requireNonNull(builder.container);
        this.stdResponses = builder.stdResponses;// can be null
        this.dockerStateChangedDate = Objects.requireNonNull(builder.dockerStateChangedDate);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public WorkerId workerId() {
        return workerId;
    }

    public InspectContainerResponse container() {
        return container;
    }

    public List<StdResponse> stdResponses() {
        return stdResponses;
    }

    public UTCZonedDateTime dockerStateChangedDate() {
        return dockerStateChangedDate;
    }

    public static final class Builder {

        private WorkerId workerId;
        private InspectContainerResponse container;
        private List<StdResponse> stdResponses;
        private UTCZonedDateTime dockerStateChangedDate;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withContainer(final InspectContainerResponse container) {
            this.container = container;
            return this;
        }

        public Builder withStdResponses(final List<StdResponse> stdResponses) {
            this.stdResponses = stdResponses;
            return this;
        }

        public Builder withDockerStateChangedDate(final UTCZonedDateTime dockerStateChangedDate) {
            this.dockerStateChangedDate = dockerStateChangedDate;
            return this;
        }

        public DockerStateChanged build() {
            return new DockerStateChanged(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerStateChanged)) return false;
        DockerStateChanged that = (DockerStateChanged) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(container, that.container) &&
                Objects.equals(stdResponses, that.stdResponses) &&
                Objects.equals(dockerStateChangedDate, that.dockerStateChangedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, container, stdResponses, dockerStateChangedDate);
    }

    @Override
    public String toString() {
        return "DockerStateChanged{" +
                "workerId=" + workerId +
                ", container=" + container +
                ", stdResponses=" + stdResponses +
                ", dockerStateChangedDate=" + dockerStateChangedDate +
                '}';
    }
}
