package com.tank.soar.worker_orchestrator.infrastructure.repository;

import com.tank.soar.worker_orchestrator.domain.LogStream;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import javax.json.JsonObject;
import java.util.Objects;

public final class LogStreamEntity implements LogStream {

    private final WorkerId workerId;
    private final LogStreamType logStreamType;
    private final String content;

    public LogStreamEntity(final WorkerId workerId, final LogStreamType logStreamType, final String content) {
        this.workerId = Objects.requireNonNull(workerId);
        this.logStreamType = Objects.requireNonNull(logStreamType);
        this.content = Objects.requireNonNull(content);
    }

    public LogStreamEntity(final JsonObject logStream) {
        this(
                new WorkerId(logStream.getString("workerId")),
                LogStreamType.valueOf(logStream.getString("logStreamType")),
                logStream.getString("content")
        );
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
        if (!(o instanceof LogStreamEntity)) return false;
        LogStreamEntity that = (LogStreamEntity) o;
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
        return "LogStreamEntity{" +
                "workerId=" + workerId +
                ", logStreamType=" + logStreamType +
                ", content='" + content + '\'' +
                '}';
    }
}
