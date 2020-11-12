package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.Objects;

public final class RetrieveWorkerStdErrCommand implements UseCaseCommand {

    private final WorkerId workerId;

    private RetrieveWorkerStdErrCommand(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
    }

    public WorkerId workerId() {
        return workerId;
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private WorkerId workerId;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public RetrieveWorkerStdErrCommand build() {
            return new RetrieveWorkerStdErrCommand(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetrieveWorkerStdErrCommand)) return false;
        RetrieveWorkerStdErrCommand that = (RetrieveWorkerStdErrCommand) o;
        return Objects.equals(workerId, that.workerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId);
    }

    @Override
    public String toString() {
        return "RetrieveWorkerStdErrCommand{" +
                "workerId=" + workerId +
                '}';
    }
}
