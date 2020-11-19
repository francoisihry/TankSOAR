package com.tank.soar.worker_orchestrator.domain.usecase;

import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.Objects;

public final class RetrieveWorkerLogsCommand implements UseCaseCommand {

    private final WorkerId workerId;
    private final Boolean stdOut;
    private final Boolean stdErr;

    private RetrieveWorkerLogsCommand(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.stdOut = Objects.requireNonNull(builder.stdOut);
        this.stdErr = Objects.requireNonNull(builder.stdErr);
    }

    public WorkerId workerId() {
        return workerId;
    }

    public Boolean stdOut() {
        return stdOut;
    }

    public Boolean stdErr() {
        return stdErr;
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private WorkerId workerId;
        private Boolean stdOut;
        private Boolean stdErr;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withStdOut(final Boolean stdOut) {
            this.stdOut = stdOut;
            return this;
        }

        public Builder withStdErr(final Boolean stdErr) {
            this.stdErr = stdErr;
            return this;
        }

        public RetrieveWorkerLogsCommand build() {
            return new RetrieveWorkerLogsCommand(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetrieveWorkerLogsCommand)) return false;
        RetrieveWorkerLogsCommand that = (RetrieveWorkerLogsCommand) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(stdOut, that.stdOut) &&
                Objects.equals(stdErr, that.stdErr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, stdOut, stdErr);
    }

    @Override
    public String toString() {
        return "RetrieveWorkerLogsCommand{" +
                "workerId=" + workerId +
                ", stdOut=" + stdOut +
                ", stdErr=" + stdErr +
                '}';
    }
}
