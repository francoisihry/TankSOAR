package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.usecase.UnableToRunScriptUseCaseException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnableToRunScriptUseCaseExceptionHandler implements ExceptionMapper<UnableToRunScriptUseCaseException> {

    @Override
    public Response toResponse(final UnableToRunScriptUseCaseException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(String.format("Unable to run script '%s'.", exception.getCause().getMessage())).build();
    }

}
