package com.tank.soar.worker_orchestrator.infrastructure.interfaces.workersLifecycle;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.infrastructure.NewWorkerEvent;
import com.tank.soar.worker_orchestrator.infrastructure.interfaces.WorkerDTO;
import io.quarkus.runtime.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServerEndpoint(value = "/workersLifecycle",
        encoders = { WorkerDTOEncoder.class })
@ApplicationScoped
@Startup
public class WorkersLifecycleSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkersLifecycleSocket.class);

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private final ExecutorService workerStateChangedExecutorService = Executors.newSingleThreadExecutor();

    @OnOpen
    public void onOpen(final Session session) {
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(final Session session) {
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        sessions.remove(session.getId());
    }

    private void broadcastToEveryone(final Worker worker) {
        LOGGER.info(String.format("broadcasting worker state changed '%s', '%d' sessions to notify", worker.toString(), sessions.size()));
        sessions.values().forEach(session ->
            session.getAsyncRemote()
                    .sendObject(new WorkerDTO(worker), result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            })
        );
    }

    void onWorkerStateChanged(@Observes final NewWorkerEvent newWorkerEvent) {
        workerStateChangedExecutorService.submit(() -> broadcastToEveryone(newWorkerEvent.worker()));
    }

// TODO  created, deleted, running, stopped...

}
