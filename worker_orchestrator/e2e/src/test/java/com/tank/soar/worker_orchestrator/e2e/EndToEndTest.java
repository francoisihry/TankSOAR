package com.tank.soar.worker_orchestrator.e2e;

import com.github.dockerjava.api.DockerClient;
import com.tank.soar.worker_orchestrator.e2e.resources.EndToEndTestResource;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalPropertiesReader;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(EndToEndTestResource.class)
public class EndToEndTest {

    @ConfigProperty(name = "tanksoar.worker_orchestrator.http.port")
    Integer workerOrchestratorExposedHttpPort;

    @ConfigProperty(name = "quarkus.datasource.workers.jdbc.url")
    String workersJdbcUrl;
    @ConfigProperty(name = "quarkus.datasource.workers.username")
    String workersUsername;
    @ConfigProperty(name = "quarkus.datasource.workers.password")
    String workersPassword;

    @TestHTTPResource("/workersLifecycle")
    URI workersLifecycleUri;

    private static final LinkedBlockingDeque<String> JSON_WORKER_LIFECYCLE_MESSAGE = new LinkedBlockingDeque<>();

    AgroalDataSource workerDataSource;

    @BeforeEach
    public void setup() throws Exception {
        RestAssured.port = workerOrchestratorExposedHttpPort;

        final Map<String,String> props=new HashMap<>();
        props.put(AgroalPropertiesReader.MAX_SIZE,"10");
        props.put(AgroalPropertiesReader.MIN_SIZE,"10");
        props.put(AgroalPropertiesReader.INITIAL_SIZE,"10");
        props.put(AgroalPropertiesReader.MAX_LIFETIME_S,"300");
        props.put(AgroalPropertiesReader.ACQUISITION_TIMEOUT_S,"30");
        props.put(AgroalPropertiesReader.JDBC_URL,workersJdbcUrl);
        props.put(AgroalPropertiesReader.PRINCIPAL,workersUsername);
        props.put(AgroalPropertiesReader.CREDENTIAL,workersPassword);

        workerDataSource = AgroalDataSource.from(new AgroalPropertiesReader()
                .readProperties(props)
                .get());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EndToEndTest.class);

    static final DockerClient dockerClient;

    static {
        dockerClient = DockerClientFactory
                .instance()
                .client();
    }

    @AfterEach
    @BeforeEach
    public void removeAllTestContainers() {
        dockerClient.listContainersCmd()
                .withLabelFilter(Collections.singleton("workerId"))
                .withShowAll(true)
                .exec()
                .stream()
                .peek(container -> LOGGER.info(String.format("Need to remove container '%s'", container.getId())))
                .forEach(container ->
                        dockerClient.removeContainerCmd(container.getId())
                                .withForce(true)
                                .exec()
                );
        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE WORKER");
            stmt.executeUpdate("TRUNCATE TABLE WORKER_EVENT");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
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
                .body("$.size()", equalTo(1));
    }

    @Test
    public void should_run_an_hello_world_script() throws Exception {
        final WorkerLifecycleClient workerLifecycleClient = new WorkerLifecycleClient(workersLifecycleUri);
        workerLifecycleClient.connect();

        final String workerId = given()
                .formParam("script", "print(\"hello world\")")
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("workerId");

        assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS)).contains(workerId).contains("CREATION_REQUESTED");
        assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS)).contains(workerId).contains("CREATED");
        assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS)).contains(workerId).contains("RUNNING");
        assertThat(JSON_WORKER_LIFECYCLE_MESSAGE.poll(10, TimeUnit.SECONDS)).contains(workerId).contains("FINISHED");

        try (final Connection con = workerDataSource.getConnection();
             final Statement stmt = con.createStatement();
             final ResultSet countWorkerRS = stmt.executeQuery("SELECT COUNT(*) FROM WORKER");
             final ResultSet countWorkerEventsRS = stmt.executeQuery("SELECT COUNT(*) FROM WORKER_EVENT")) {
            countWorkerRS.next();
            assertThat(countWorkerRS.getLong(1)).isEqualTo(1l);
            countWorkerEventsRS.next();
            assertThat(countWorkerEventsRS.getLong(1)).isEqualTo(4l);
        }
        // ici le log !!! trop compliqué. Je dois connaitre l'id avant pour l'injecter dans l'url ...

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
                                .formParam("stdOut", Boolean.TRUE)
                                .formParam("stdErr", Boolean.FALSE)
                                .post("/workers/" + workerId + "/logs")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().path("[0].content").equals("hello world")
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
    public void should_get_std_err_log() {
        // Given
        final String script = Stream.of("import sys;",
                "print('bye bye world', file=sys.stderr);")
                .collect(Collectors.joining());
        final String workerId = given()
                .formParam("script", script)
                .when()
                .post("/workers/runScript")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("workerId");

        // When && Then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> {
                    int nbOfLogs = given()
                                .formParam("stdOut", Boolean.FALSE)
                                .formParam("stdErr", Boolean.TRUE)
                                .when()
                                .post("/workers/" + workerId + "/logs")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract()
                                .path("$.size()");
                    return nbOfLogs > 0l;
                });

        given()
                .formParam("stdOut", Boolean.FALSE)
                .formParam("stdErr", Boolean.TRUE)
                .when()
                .post("/workers/" + workerId + "/logs")
                .then()
                .log().all()
                .statusCode(200)
                .body("[0].content", equalTo("bye bye world"));
    }

    public static class WorkerLifecycleClient extends WebSocketClient {

        public WorkerLifecycleClient(final URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(final ServerHandshake serverHandshake) {
            System.out.println("opened");
        }

        @Override
        public void onMessage(final String jsonWorkerLifecycleMessage) {
            LOGGER.info(String.format("Received message '%s'", jsonWorkerLifecycleMessage));
            JSON_WORKER_LIFECYCLE_MESSAGE.add(jsonWorkerLifecycleMessage);
        }

        @Override
        public void onClose(final int i, final String s, final boolean b) {

        }

        @Override
        public void onError(final Exception e) {

        }
    }
    // TODO faire des tests de performance en bombardant la creation de containers ...
}
