package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.tank.soar.worker_orchestrator.domain.LogStream;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.Objects;

public final class StdResponse implements LogStream {
    private final WorkerId workerId;
    private final LogStreamType logStreamType;
    private final String content;

    private StdResponse(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.logStreamType = Objects.requireNonNull(builder.logStreamType);
        this.content = Objects.requireNonNull(builder.content);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private WorkerId workerId;
        private LogStreamType logStreamType;
        private String content;

        public Builder withWorkerId(final WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withLogStreamType(final LogStreamType logStreamType) {
            this.logStreamType = logStreamType;
            return this;
        }

        public Builder withContent(final String content) {
            this.content = content;
            return this;
        }

        public StdResponse build() {
            return new StdResponse(this);
        }
    }

    @Override
    public WorkerId workerId() {
        return workerId;
    }

    @Override
    public LogStreamType logStreamType() {
        return logStreamType;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StdResponse)) return false;
        StdResponse that = (StdResponse) o;
        return Objects.equals(workerId, that.workerId) &&
                logStreamType == that.logStreamType &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, logStreamType, content);
    }
}
