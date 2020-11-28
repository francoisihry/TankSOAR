package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.usecase.WorkerAlreadyDeletedUseCaseException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WorkerAlreadyDeletedUseCaseExceptionHandler implements ExceptionMapper<WorkerAlreadyDeletedUseCaseException> {

    @Override
    public Response toResponse(final WorkerAlreadyDeletedUseCaseException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(String.format("Worker '%s' already deleted.", exception.workerId().id())).build();
    }

}
