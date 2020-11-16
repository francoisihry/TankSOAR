package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.usecase.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/workers")
public class WorkerEndpoint {

    private final GetWorkerUseCase getWorkerUseCase;
    private final ListWorkersUseCase listWorkersUseCase;
    private final RetrieveWorkerStdErrUseCase retrieveWorkerStdErrUseCase;
    private final RetrieveWorkerStdOutUseCase retrieveWorkerStdOutUseCase;
    private final RunScriptUseCase runScriptUseCase;

    public WorkerEndpoint(final GetWorkerUseCase getWorkerUseCase,
                          final ListWorkersUseCase listWorkersUseCase,
                          final RetrieveWorkerStdErrUseCase retrieveWorkerStdErrUseCase,
                          final RetrieveWorkerStdOutUseCase retrieveWorkerStdOutUseCase,
                          final RunScriptUseCase runScriptUseCase) {
        this.getWorkerUseCase = Objects.requireNonNull(getWorkerUseCase);
        this.listWorkersUseCase = Objects.requireNonNull(listWorkersUseCase);
        this.retrieveWorkerStdErrUseCase = Objects.requireNonNull(retrieveWorkerStdErrUseCase);
        this.retrieveWorkerStdOutUseCase = Objects.requireNonNull(retrieveWorkerStdOutUseCase);
        this.runScriptUseCase = Objects.requireNonNull(runScriptUseCase);
    }

    @GET
    @Path("/{workerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerDTO getWorker(@PathParam("workerId") final String workerId) throws UseCaseException {
        return Optional.of(getWorkerUseCase.execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId(workerId)).build()))
                .map(WorkerDTO::new)
                .get();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkerDTO> getWorkers() throws UseCaseException {
        return listWorkersUseCase.execute(new ListWorkersCommand())
                .stream()
                .map(WorkerDTO::new)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{workerId}/stdOut")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerLogDTO getWorkerStdOut(@PathParam("workerId") final String workerId) throws UseCaseException {
        return new WorkerLogDTO(retrieveWorkerStdOutUseCase.execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId(workerId)).build()));
    }

    @GET
    @Path("/{workerId}/stdErr")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerLogDTO getWorkerStdErr(@PathParam("workerId") final String workerId) throws UseCaseException {
        return new WorkerLogDTO(retrieveWorkerStdErrUseCase.execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId(workerId)).build()));
    }

    @POST
    @Path("/runScript")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerDTO runScript(@FormParam("script") final String script) throws UnableToRunScriptUseCaseException {
        return Optional.of(runScriptUseCase.execute(RunScriptCommand.newBuilder().withScript(script).build()))
                .map(WorkerDTO::new)
                .get();
    }

}
