package com.tank.soar.worker_orchestrator.infrastructure.interfaces.workersLifecycle;

import com.tank.soar.worker_orchestrator.domain.Worker;
import com.tank.soar.worker_orchestrator.infrastructure.container.WorkerStateChanged;
import com.tank.soar.worker_orchestrator.infrastructure.interfaces.WorkerDTO;
import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/workersLifecycle",
        encoders = { WorkerDTOEncoder.class })
@ApplicationScoped
@Startup
public class WorkersLifecycleSocket {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

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
        sessions.values().forEach(session ->
            session.getAsyncRemote()
                    .sendObject(new WorkerDTO(worker), result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            })
        );
    }

    void onWorkerStateChanged(@Observes final WorkerStateChanged workerStateChanged) {
        broadcastToEveryone(workerStateChanged.worker());
    }

// TODO  created, deleted, running, stopped...

}
