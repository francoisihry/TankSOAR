package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.usecase.UnknownWorkerUseCaseException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnknownWorkerUseCaseExceptionHandler implements ExceptionMapper<UnknownWorkerUseCaseException> {

    @Override
    public Response toResponse(final UnknownWorkerUseCaseException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.TEXT_PLAIN)
                .entity(String.format("Unknown worker '%s'.", exception.unknownWorkerId().id())).build();
    }

}
