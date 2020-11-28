package com.tank.soar.worker_orchestrator.infrastructure.interfaces;

import com.tank.soar.worker_orchestrator.domain.*;
import com.tank.soar.worker_orchestrator.domain.usecase.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import io.restassured.module.jsv.JsonSchemaValidator;

import java.time.Month;
import java.util.Arrays;
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
    private RetrieveWorkerLogsUseCase retrieveWorkerLogsUseCase;

    @InjectMock
    private RunScriptUseCase runScriptUseCase;

    @InjectMock
    private StopRunningScriptUseCase stopRunningScriptUseCase;

    @Test
    public void should_get_worker() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        doReturn(Boolean.TRUE).when(worker).hasFinished();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
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
                .body("lastUpdateStateDate", equalTo("2020-09-01T10:10:00Z"))
                .body("workerStatus", equalTo("FINISHED"))
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
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
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
                .body("[0].lastUpdateStateDate", equalTo("2020-09-01T10:10:00Z"))
                .body("[0].workerStatus", equalTo("FINISHED"))
                .body("[0].hasFinished", equalTo(Boolean.TRUE));
    }

    @Test
    public void should_get_worker_std_out_log() {
        // Given
        final LogStream logStreamStdOut = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdOut).workerId();
        doReturn("stdOut").when(logStreamStdOut).content();
        doReturn(LogStreamType.STDOUT).when(logStreamStdOut).logStreamType();
        doCallRealMethod().when(logStreamStdOut).toJsonStringRepresentation();
        doReturn(Collections.singletonList(logStreamStdOut))
                .when(retrieveWorkerLogsUseCase)
                .execute(RetrieveWorkerLogsCommand.newBuilder()
                        .withWorkerId(new WorkerId("id"))
                        .withStdOut(Boolean.TRUE)
                        .withStdErr(Boolean.FALSE).build());

        // When && Then
        given()
                .formParam("stdOut", Boolean.TRUE)
                .formParam("stdErr", Boolean.FALSE)
                .when()
                .post("/workers/id/logs")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/logs.json"))
                .body("[0].content", equalTo("stdOut"))
                .body("[0].logStreamType", equalTo("STDOUT"))
                .body("[0].workerId", equalTo("id"));
    }

    @Test
    public void should_get_worker_std_err_log() {
        // Given
        final LogStream logStreamStdErr = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdErr).workerId();
        doReturn("stdErr").when(logStreamStdErr).content();
        doReturn(LogStreamType.STDERR).when(logStreamStdErr).logStreamType();
        doCallRealMethod().when(logStreamStdErr).toJsonStringRepresentation();
        doReturn(Collections.singletonList(logStreamStdErr))
                .when(retrieveWorkerLogsUseCase)
                .execute(RetrieveWorkerLogsCommand.newBuilder()
                        .withWorkerId(new WorkerId("id"))
                        .withStdOut(Boolean.FALSE)
                        .withStdErr(Boolean.TRUE).build());
        // When && Then
        given()
                .formParam("stdOut", Boolean.FALSE)
                .formParam("stdErr", Boolean.TRUE)
                .when()
                .post("/workers/id/logs")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/logs.json"))
                .body("[0].content", equalTo("stdErr"))
                .body("[0].logStreamType", equalTo("STDERR"))
                .body("[0].workerId", equalTo("id"));
    }

    @Test
    public void should_get_std_out_and_std_err_logs_in_order() throws Exception {
        // Given
        final LogStream logStreamStdOut = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdOut).workerId();
        doReturn("stdOut").when(logStreamStdOut).content();
        doReturn(LogStreamType.STDOUT).when(logStreamStdOut).logStreamType();
        doCallRealMethod().when(logStreamStdOut).toJsonStringRepresentation();
        final LogStream logStreamStdErr = mock(LogStream.class);
        doReturn(new WorkerId("id")).when(logStreamStdErr).workerId();
        doReturn("stdErr").when(logStreamStdErr).content();
        doReturn(LogStreamType.STDERR).when(logStreamStdErr).logStreamType();
        doCallRealMethod().when(logStreamStdErr).toJsonStringRepresentation();

        doReturn(Arrays.asList(logStreamStdOut, logStreamStdErr))
                .when(retrieveWorkerLogsUseCase)
                .execute(RetrieveWorkerLogsCommand.newBuilder()
                        .withWorkerId(new WorkerId("id"))
                        .withStdOut(Boolean.TRUE)
                        .withStdErr(Boolean.TRUE).build());
        // When && Then
        given()
                .formParam("stdOut", Boolean.TRUE)
                .formParam("stdErr", Boolean.TRUE)
                .when()
                .post("/workers/id/logs")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/logs.json"))
                .body("[0].content", equalTo("stdOut"))
                .body("[0].logStreamType", equalTo("STDOUT"))
                .body("[0].workerId", equalTo("id"))
                .body("[1].content", equalTo("stdErr"))
                .body("[1].logStreamType", equalTo("STDERR"))
                .body("[1].workerId", equalTo("id"));
    }

    @Test
    public void should_get_worker_logs_return_expected_response_when_worker_does_not_exist() {
        // Given
        doThrow(new UnknownWorkerUseCaseException(new WorkerId("id"))).when(retrieveWorkerLogsUseCase)
                .execute(RetrieveWorkerLogsCommand.newBuilder()
                        .withWorkerId(new WorkerId("id"))
                        .withStdOut(Boolean.TRUE)
                        .withStdErr(Boolean.TRUE).build());

        // When && Then
        given()
                .formParam("stdOut", Boolean.TRUE)
                .formParam("stdErr", Boolean.TRUE)
                .when()
                .post("/workers/id/logs")
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
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
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
                .body("lastUpdateStateDate", equalTo("2020-09-01T10:10:00Z"))
                .body("workerStatus", equalTo("FINISHED"))
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

    @Test
    public void should_stop_running_script() {
        // Given
        final Worker worker = mock(Worker.class);
        doReturn(new WorkerId("id")).when(worker).workerId();
        doReturn(WorkerStatus.FINISHED).when(worker).workerStatus();
        doReturn(Boolean.TRUE).when(worker).hasFinished();
        doReturn(UTCZonedDateTime.of(2020, Month.SEPTEMBER, 1, 10, 10, 00)).when(worker).lastUpdateStateDate();
        doReturn(Source.CONTAINER).when(worker).source();
        doReturn(worker).when(stopRunningScriptUseCase)
                .execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .post("/workers/id/stopRunningScript")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/worker.json"))
                .body("workerId", equalTo("id"))
                .body("lastUpdateStateDate", equalTo("2020-09-01T10:10:00Z"))
                .body("workerStatus", equalTo("FINISHED"))
                .body("hasFinished", equalTo(Boolean.TRUE));
    }

    @Test
    public void should_stop_running_script_return_expected_response_when_the_worker_does_not_exist() {
        // Given
        doThrow(new UnknownWorkerUseCaseException(new WorkerId("id"))).when(stopRunningScriptUseCase)
                .execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .post("/workers/id/stopRunningScript")
                .then()
                .log().all()
                .statusCode(404)
                .body(equalTo("Unknown worker 'id'."));
    }

    @Test
    public void should_stop_running_script_return_expected_response_when_the_worker_is_already_deleted() {
        // Given
        doThrow(new WorkerAlreadyDeletedUseCaseException(new WorkerId("id"))).when(stopRunningScriptUseCase)
                .execute(StopRunningScriptCommand.newBuilder().withWorkerId(new WorkerId("id")).build());

        // When && Then
        given()
                .when()
                .post("/workers/id/stopRunningScript")
                .then()
                .log().all()
                .statusCode(400)
                .body(equalTo("Worker 'id' already deleted."));
    }

}
