package com.tank.soar.worker_orchestrator.infrastructure.container;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoggingResultCallbackAdapter extends ResultCallback.Adapter<Frame> {

    public static class StdResponse {
        private final StreamType streamType;
        private final String response;

        public StdResponse(final StreamType streamType, final String response) {
            this.streamType = streamType;
            this.response = response;
        }

        public StdResponse(final StreamType streamType, final byte[] response) {
            this(streamType, new String(response));
        }

        public StdResponse(final Frame frame) {
            this(frame.getStreamType(), frame.getPayload());
        }

        public StreamType getStreamType() {
            return streamType;
        }

        public String getResponse() {
            return response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StdResponse)) return false;
            StdResponse that = (StdResponse) o;
            return streamType == that.streamType &&
                    Objects.equals(response, that.response);
        }

        @Override
        public int hashCode() {
            return Objects.hash(streamType, response);
        }

        @Override
        public String toString() {
            return "StdResponse{" +
                    "streamType=" + streamType +
                    ", response='" + response + '\'' +
                    '}';
        }
    }

    private List<StdResponse> stdResponses = new ArrayList<>();

    @Override
    public void onNext(final Frame object) {
        this.stdResponses.add(new StdResponse(object));
    }

    public List<StdResponse> getStdResponses() {
        return stdResponses;
    }
}
