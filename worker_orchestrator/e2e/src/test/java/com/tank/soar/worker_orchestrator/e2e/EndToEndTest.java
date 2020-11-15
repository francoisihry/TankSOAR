package com.tank.soar.worker_orchestrator.e2e;

import com.tank.soar.worker_orchestrator.e2e.resources.EndToEndTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@QuarkusTestResource(EndToEndTestResource.class)
public class EndToEndTest {

    @ConfigProperty(name = "tanksoar.worker_orchestrator.http.port")
    private Integer workerOrchestratorExposedHttpPort;

    @BeforeEach
    public void setup() {
        RestAssured.port = workerOrchestratorExposedHttpPort;
    }

    @Test
    public void should_list_all_workers() {
        // Given

        // When
        given()
                .formParam("script", "print(\"hello world\")")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("workerId");

        // Then
        given()
                .when()
                .get("/workers")
                .then()
                .log().all()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void should_run_an_hello_world_script() {
        final String workerId = given()
                .formParam("script", "print(\"hello world\")")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("workerId");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() ->
                        given()
                                .when()
                                .get("/workers/" + workerId)
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().path("workerStatus").equals("FINISHED")
                );

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() ->
                        given()
                                .when()
                                .get("/workers/" + workerId + "/stdOut")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().path("log").equals("hello world\n")
                );
    }

    @Test
    public void should_rest_exception_handler_works() {
        // When && Then
        given()
                .when()
                .get("/workers/" + UUID.randomUUID().toString())
                .then()
                .log().all()
                .statusCode(404);
    }

    @Test
    public void should_get_std_err() {
        // Given
        final String workerId = given()
                .formParam("script", "import sys\nprint(\"bye bye world\", file=sys.stderr)")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("workerId");

        // When && Then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() ->
                        given()
                                .when()
                                .get("/workers/" + workerId + "/stdErr")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().path("log").equals("bye bye world\n")
                );
    }

    // TODO faire des tests de performance en bombardant la creation de containers ...

}
