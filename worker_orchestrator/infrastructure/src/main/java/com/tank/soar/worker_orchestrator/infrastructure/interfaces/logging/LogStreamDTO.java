package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import com.tank.soar.worker_orchestrator.domain.LogStream;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public final class LogStreamDTO implements LogStream {

    private final WorkerId workerId;
    private final LogStreamType logStreamType;
    private final String content;

    private LogStreamDTO(final Builder builder) {
        this.workerId = Objects.requireNonNull(builder.workerId);
        this.logStreamType = Objects.requireNonNull(builder.logStreamType);
        this.content = Objects.requireNonNull(builder.content);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private WorkerId workerId;
        private LogStreamType logStreamType;
        private String content;

        public Builder withWorkerId(WorkerId workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder withLogStreamType(LogStreamType logStreamType) {
            this.logStreamType = logStreamType;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public LogStreamDTO build() {
            return new LogStreamDTO(this);
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

    public String getWorkerId() {
        return workerId.id();
    }

    public LogStreamType getLogStreamType() {
        return logStreamType;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogStreamDTO)) return false;
        LogStreamDTO that = (LogStreamDTO) o;
        return Objects.equals(workerId, that.workerId) &&
                logStreamType == that.logStreamType &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, logStreamType, content);
    }

    @Override
    public String toString() {
        return "LogStreamDTO{" +
                "workerId=" + workerId +
                ", logStreamType=" + logStreamType +
                ", content='" + content + '\'' +
                '}';
    }
}
