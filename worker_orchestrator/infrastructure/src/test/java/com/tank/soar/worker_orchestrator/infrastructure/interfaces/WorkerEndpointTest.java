package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.domain.usecase.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import io.restassured.module.jsv.JsonSchemaValidator;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@QuarkusTest
public class WorkerEndpointTest {

    @InjectMock
    private GetWorkerUseCase getWorkerUseCase;

    @InjectMock
    private ListWorkersUseCase listWorkersUseCase;

    @InjectMock
    private RetrieveWorkerStdErrUseCase retrieveWorkerStdErrUseCase;

    @InjectMock
    private RetrieveWorkerStdOutUseCase retrieveWorkerStdOutUseCase;

    @InjectMock
    private RunScriptUseCase runScriptUseCase;

    @Test
    public void should_get_worker() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        doReturn(Boolean.TRUE).when(worker).hasFinished();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00)).when(worker).createdAt();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
        doReturn(Source.CONTAINER).when(worker).source();
        doReturn(worker).when(getWorkerUseCase).execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/worker.json"))
                .body("workerId", equalTo("id"))
                .body("lastUpdateStateDate", equalTo("2020-09-01T10:10:00"))
                .body("workerStatus", equalTo("FINISHED"))
                .body("createdAt", equalTo("2020-09-01T10:00:00"))
                .body("hasFinished", equalTo(Boolean.TRUE));
    }

    @Test
    public void should_get_worker_return_expected_response_when_worker_does_not_exist() {
        // Given
        doThrow(new UnknownWorkerUseCaseException(new WorkerId("id"))).when(getWorkerUseCase).execute(GetWorkerCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id")
                .then()
                .log().all()
                .statusCode(404)
                .body(equalTo("Unknown worker 'id'."));
    }

    @Test
    public void should_list_all_workers() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        doReturn(Boolean.TRUE).when(worker).hasFinished();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00)).when(worker).createdAt();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
        doReturn(Source.CONTAINER).when(worker).source();
        doReturn(Collections.singletonList(worker)).when(listWorkersUseCase).execute(any());

        // When && Then
        given()
                .when()
                .get("/workers")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/workers.json"))
                .body("[0].workerId", equalTo("id"))
                .body("[0].lastUpdateStateDate", equalTo("2020-09-01T10:10:00"))
                .body("[0].workerStatus", equalTo("FINISHED"))
                .body("[0].createdAt", equalTo("2020-09-01T10:00:00"))
                .body("[0].hasFinished", equalTo(Boolean.TRUE));
    }

    @Test
    public void should_get_worker_std_out() {
        // Given
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(new WorkerId("id")).when(workerLog).workerId();
        doReturn(Boolean.TRUE).when(workerLog).hasFinishedProducingLog();
        doReturn("hello stdout").when(workerLog).log();
        doReturn(workerLog).when(retrieveWorkerStdOutUseCase).execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id/stdOut")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/stdOut.json"))
                .body("workerId", equalTo("id"))
                .body("hasFinishedProducingLog", equalTo(Boolean.TRUE))
                .body("log", equalTo("hello stdout"));
    }

    @Test
    public void should_get_worker_std_out_return_expected_response_when_worker_does_not_exist() {
        // Given
        doThrow(new UnknownWorkerUseCaseException(new WorkerId("id"))).when(retrieveWorkerStdOutUseCase).execute(RetrieveWorkerStdOutCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id/stdOut")
                .then()
                .log().all()
                .statusCode(404)
                .body(equalTo("Unknown worker 'id'."));
    }

    @Test
    public void should_get_worker_std_err() {
        // Given
        final WorkerLog workerLog = mock(WorkerLog.class);
        doReturn(new WorkerId("id")).when(workerLog).workerId();
        doReturn(Boolean.TRUE).when(workerLog).hasFinishedProducingLog();
        doReturn("hello stderr").when(workerLog).log();
        doReturn(workerLog).when(retrieveWorkerStdErrUseCase).execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id/stdErr")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/stdErr.json"))
                .body("workerId", equalTo("id"))
                .body("hasFinishedProducingLog", equalTo(Boolean.TRUE))
                .body("log", equalTo("hello stderr"));
    }

    @Test
    public void should_get_worker_std_err_return_expected_response_when_worker_does_not_exist() {
        // Given
        doThrow(new UnknownWorkerUseCaseException(new WorkerId("id"))).when(retrieveWorkerStdErrUseCase).execute(RetrieveWorkerStdErrCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .get("/workers/id/stdErr")
                .then()
                .log().all()
                .statusCode(404)
                .body(equalTo("Unknown worker 'id'."));
    }

    @Test
    public void should_run_script() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        doReturn(Boolean.TRUE).when(worker).hasFinished();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 00, 00)).when(worker).createdAt();
        doReturn(LocalDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
        doReturn(Source.CONTAINER).when(worker).source();
        doReturn(worker).when(runScriptUseCase).execute(RunScriptCommand.newBuilder().withScript("print(\"hello world\")").build());

        // When && Then
        given()
                .formParam("script", "print(\"hello world\")")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/worker.json"))
                .body("workerId", equalTo("id"))
                .body("lastUpdateStateDate", equalTo("2020-09-01T10:10:00"))
                .body("workerStatus", equalTo("FINISHED"))
                .body("createdAt", equalTo("2020-09-01T10:00:00"))
                .body("hasFinished", equalTo(Boolean.TRUE));
    }

    @Test
    public void should_run_script_return_expected_response_when_the_script_was_unable_to_run() {
        // Given
        doThrow(new UnableToRunScriptUseCaseException(new UnknownWorkerException(new WorkerId("id")))).when(runScriptUseCase)
                .execute(RunScriptCommand.newBuilder().withScript("print(\"hello world\")").build());

        // When && Then
        given()
                .formParam("script", "print(\"hello world\")")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(400)
                .body(equalTo("Unable to run script 'null'."));
    }

}
