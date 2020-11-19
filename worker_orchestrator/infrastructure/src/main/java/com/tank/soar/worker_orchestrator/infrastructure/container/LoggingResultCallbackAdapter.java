package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.tank.soar.worker_orchestrator.domain.LogStream;
import com.tank.soar.worker_orchestrator.domain.LogStreamType;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoggingResultCallbackAdapter extends ResultCallback.Adapter<Frame> {

    public static class StdResponse implements LogStream {
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

    }

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
}
