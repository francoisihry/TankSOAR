package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import com.github.dockerjava.api.DockerClient;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/logs/{workerId}",
        encoders = { LoggingFrameDTOEncoder.class })
@ApplicationScoped
public class LoggingFrameSocket {

    private final Map<String, LoggingFrameSocketResultCallbackAdapter> loggingFrameSocketResultCallbackAdapterBySessionId
            = new ConcurrentHashMap<>();

    private final DockerClient dockerClient;

    public LoggingFrameSocket(final DockerClient dockerClient) {
        this.dockerClient = Objects.requireNonNull(dockerClient);
    }

    @OnOpen
    public void onOpen(final Session session, final @PathParam("workerId") String workerId) {
        final LoggingFrameSocketResultCallbackAdapter loggingFrameSocketResultCallbackAdapter
                = new LoggingFrameSocketResultCallbackAdapter(new WorkerId(workerId), session);
        final String sessionId = session.getId();
        loggingFrameSocketResultCallbackAdapterBySessionId.put(sessionId, loggingFrameSocketResultCallbackAdapter);
        try {
            this.dockerClient.logContainerCmd(workerId)
                    .withStdErr(true)
                    .withStdOut(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(loggingFrameSocketResultCallbackAdapter).awaitCompletion();
        } catch (InterruptedException e) {
            // TODO
            e.printStackTrace();
        } finally {
            loggingFrameSocketResultCallbackAdapterBySessionId.remove(sessionId);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("workerId") String workerId) {
        final String sessionId = session.getId();
        try {
            if (loggingFrameSocketResultCallbackAdapterBySessionId.containsKey(sessionId)) {
                loggingFrameSocketResultCallbackAdapterBySessionId.get(sessionId).close();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            loggingFrameSocketResultCallbackAdapterBySessionId.remove(sessionId);
        }
    }

    @OnError
    public void onError(Session session, @PathParam("workerId") String workerId, Throwable throwable) {
        final String sessionId = session.getId();
        try {
            if (loggingFrameSocketResultCallbackAdapterBySessionId.containsKey(sessionId)) {
                loggingFrameSocketResultCallbackAdapterBySessionId.get(sessionId).close();
            }
        } catch (IOException ioException) {
            loggingFrameSocketResultCallbackAdapterBySessionId.remove(sessionId);
        }
    }

}
