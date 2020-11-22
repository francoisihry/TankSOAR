package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LoggingResultCallbackAdapter extends ResultCallback.Adapter<Frame> {

    private final WorkerId workerId;

    public LoggingResultCallbackAdapter(final WorkerId workerId) {
        this.workerId = Objects.requireNonNull(workerId);
    }

    private List<StdResponse> stdResponses = new ArrayList<>();

    @Override
    public void onNext(final Frame frame) {
        this.stdResponses.add(StdResponse
                .newBuilder()
                .withWorkerId(workerId)
                .withLogStreamType(ContainerUtils.mapFromStreamType(frame.getStreamType()))
                .withContent(new String(frame.getPayload()).replace("\r", "")
                        .replace("\n", ""))
                .build());
    }

    public List<StdResponse> getStdResponses() {
        return stdResponses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoggingResultCallbackAdapter)) return false;
        LoggingResultCallbackAdapter that = (LoggingResultCallbackAdapter) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(stdResponses, that.stdResponses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, stdResponses);
    }
}
