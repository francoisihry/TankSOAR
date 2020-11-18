package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.tank.soar.worker_orchestrator.domain.WorkerId;
import org.apache.commons.lang3.Validate;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Objects;

public final class LoggingFrameDTO {

    private final StreamType streamType;
    private final String payload;
    private final WorkerId workerId;

    public LoggingFrameDTO(final Frame frame, final WorkerId workerId) {
        Validate.notNull(frame);
        Validate.notNull(workerId);
        this.streamType = frame.getStreamType();
        this.payload = new String(frame.getPayload());
        this.workerId = workerId;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("workerId", workerId.id())
                .add("streamType", streamType.name())
                .add("payload", payload)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoggingFrameDTO)) return false;
        LoggingFrameDTO that = (LoggingFrameDTO) o;
        return streamType == that.streamType &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(workerId, that.workerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamType, payload, workerId);
    }

    @Override
    public String toString() {
        return "LoggingFrameDTO{" +
                "streamType=" + streamType +
                ", payload='" + payload + '\'' +
                ", workerId=" + workerId +
                '}';
    }
}
