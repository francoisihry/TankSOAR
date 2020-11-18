package com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.tank.soar.worker_orchestrator.domain.WorkerId;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Objects;

public class LoggingFrameSocketResultCallbackAdapter extends ResultCallback.Adapter<Frame> {

    private final WorkerId workerId;
    private final Session session;

    public LoggingFrameSocketResultCallbackAdapter(final WorkerId workerId, final Session session) {
        this.workerId = Objects.requireNonNull(workerId);
        this.session = Objects.requireNonNull(session);
    }

    @Override
    public void onError(Throwable throwable) {
        super.onError(throwable);
        try {
            session.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onComplete() {
        super.onComplete();
        try {
            session.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onNext(final Frame object) {
        session.getAsyncRemote().sendObject(new LoggingFrameDTO(object, workerId), result ->  {
            if (result.getException() != null) {
                System.out.println("Unable to send message: " + result.getException());
            }
        });
    }

}
