package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.WorkerId;
import com.tank.soar.worker_orchestrator.domain.usecase.*;
import com.tank.soar.worker_orchestrator.infrastructure.interfaces.logging.LogStreamDTO;

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
    private final RetrieveWorkerLogsUseCase retrieveWorkerLogsUseCase;
    private final RunScriptUseCase runScriptUseCase;
    private final StopRunningScriptUseCase stopRunningScriptUseCase;

    public WorkerEndpoint(final GetWorkerUseCase getWorkerUseCase,
                          final ListWorkersUseCase listWorkersUseCase,
                          final RetrieveWorkerLogsUseCase retrieveWorkerLogsUseCase,
                          final RunScriptUseCase runScriptUseCase,
                          final StopRunningScriptUseCase stopRunningScriptUseCase) {
        this.getWorkerUseCase = Objects.requireNonNull(getWorkerUseCase);
        this.listWorkersUseCase = Objects.requireNonNull(listWorkersUseCase);
        this.retrieveWorkerLogsUseCase = Objects.requireNonNull(retrieveWorkerLogsUseCase);
        this.runScriptUseCase = Objects.requireNonNull(runScriptUseCase);
        this.stopRunningScriptUseCase = Objects.requireNonNull(stopRunningScriptUseCase);
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

    @POST
    @Path("/{workerId}/logs")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogStreamDTO> getLogs(@PathParam("workerId") final String workerId,
                                      @FormParam("stdOut") final boolean stdOut,
                                      @FormParam("stdErr") final boolean stdErr) {
        final RetrieveWorkerLogsCommand retrieveWorkerLogsCommand = RetrieveWorkerLogsCommand.newBuilder()
                .withWorkerId(new WorkerId(workerId))
                .withStdOut(stdOut)
                .withStdErr(stdErr)
                .build();
        return retrieveWorkerLogsUseCase.execute(retrieveWorkerLogsCommand)
                .stream()
                .map(logStream -> LogStreamDTO.newBuilder()
                        .withWorkerId(logStream.workerId())
                        .withLogStreamType(logStream.logStreamType())
                        .withContent(logStream.content())
                        .build()
                )
                .collect(Collectors.toList());
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

    @POST
    @Path("/{workerId}/stopRunningScript")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerDTO stopRunningScript(@PathParam("workerId") final String workerId) throws UseCaseException {
        return Optional.of(stopRunningScriptUseCase.execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId(workerId)).build()))
                .map(WorkerDTO::new)
                .get();
    }

}
