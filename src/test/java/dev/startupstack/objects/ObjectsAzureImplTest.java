package dev.startupstack.objects;

import static dev.startupstack.storageservice.Constants.OBJECTS_URL;
import static dev.startupstack.storageservice.Constants.REPOSITORIES_URL;
import static dev.startupstack.TestUtils.testUserAccessToken;
import static dev.startupstack.TestUtils.testAdminAccessToken;
import static dev.startupstack.TestUtils.invalidUserAccessToken;
import static dev.startupstack.TestUtils.testUserTenantID;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import dev.startupstack.TestUtils;
import dev.startupstack.storageservice.repostitories.models.RepositoriesModel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;

/**
 * ObjectsTest
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class ObjectsAzureImplTest {

    private static final Logger LOG = Logger.getLogger(ObjectsAzureImplTest.class);

    Random random = new Random();

    String containerName = "objecttestcontainer-" + random.nextInt(1024);
    String tenantID;

    String textObjectName = "testfile.txt";
    String binaryObjectName = "testfile-gzip.txt.gz";
    String localFilePath = "./src/main/resources/tests/";

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.keycloak.url")
    String keycloakURL;

    @BeforeAll
    void initialize() {
        LOG.info("Initializing ObjectAzureImplTest ...");
        TestUtils.generateAccessTokens(keycloakURL);

        this.tenantID = testUserTenantID;
        
        Response response = given().port(8081)
            .auth().preemptive().oauth2(testAdminAccessToken)
            .body(new RepositoriesModel(this.containerName))
            .contentType(ContentType.JSON)
        .when().post(REPOSITORIES_URL)
        .then().extract().response();

        if (response.statusCode() != Status.CREATED.getStatusCode()) {
            LOG.errorf("Error while creating test repo: %s ", response.getBody().asString());
            fail();
        }

    }

    @Test
    @Order(1)
    void testThatUserCanUploadObjects() throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(localFilePath + this.textObjectName));
        MultiPartSpecification content = new MultiPartSpecBuilder(data).controlName("object").fileName(this.textObjectName).build();

        byte[] binaryData = Files.readAllBytes(Paths.get(localFilePath + this.binaryObjectName));
        MultiPartSpecification binaryContent = new MultiPartSpecBuilder(binaryData).controlName("object").fileName(this.binaryObjectName).build();

        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .multiPart(content)
            .multiPart(binaryContent)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .formParam("object", data)
        .when()
            .post("/upload/{repository}")
        .then()
            .statusCode(Status.CREATED.getStatusCode());
     
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .multiPart(binaryContent)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .formParam("object", data)
        .when()
            .post("/upload/{repository}")
        .thenReturn();

    }
    @Test
    @Order(2)
    void testThatObjectCanBeDownloaded() throws IOException {
        byte[] downloadedObject = given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/download/{repository}/{name}").asByteArray();

        byte[] localFile = Files.readAllBytes(Paths.get(localFilePath + this.textObjectName));
        assertArrayEquals(localFile, downloadedObject);
    }

    @Test
    @Order(3)
    void testThatBinaryObjectCanBeDownloaded() throws IOException {
        byte[] downloadedObject = given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.binaryObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/download/{repository}/{name}").asByteArray();

        byte[] localFile = Files.readAllBytes(Paths.get(localFilePath + this.binaryObjectName));
        assertArrayEquals(localFile, downloadedObject);
    }

    @Test
    @Order(4)
    void testThatObjectInfoCanBeRetrieved() {
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/{repository}/{name}")
        .then()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @Order(5)
    void testThatObjectCanBeDeleted() {
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
        .when().delete("/{repository}/{name}")
        .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }
    @Test
    void testThatUnauthorizedUsersCannotAccessResources() throws IOException {

        byte[] data = Files.readAllBytes(Paths.get(localFilePath + this.textObjectName));
        MultiPartSpecification content = new MultiPartSpecBuilder(data).controlName("object").fileName(this.textObjectName).build();

        given()
            .multiPart(content)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .formParam("object", data)
        .when()
            .post("/upload/{repository}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());

        given()
            .pathParam("repository", this.containerName)
            .pathParam("name", this.binaryObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/download/{repository}/{name}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());

        given()
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/{repository}/{name}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());

        given()
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
        .when().delete("/{repository}/{name}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @Order(6)
    void testThatAuthorizedUserCannotUploadDataToUnauthorizedRepository() throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(localFilePath + this.textObjectName));
        MultiPartSpecification content = new MultiPartSpecBuilder(data).controlName("object").fileName(this.textObjectName).build();

        given()
            .auth().preemptive().oauth2(invalidUserAccessToken)
            .multiPart(content)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .formParam("object", data)
        .when()
            .post("/upload/{repository}")
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @Order(6)
    void testThatAuthorizedUserCannotDeleteDataFromUnauthorizedRepository() {
        given()
            .auth().preemptive().oauth2(invalidUserAccessToken)
            .basePath(OBJECTS_URL)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
        .when().delete("/{repository}/{name}")
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @Order(6)
    void testThatAuthorizedUserCannotReadDataFromUnauthorizedRepository() {

        given()
            .auth().preemptive().oauth2(invalidUserAccessToken)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.textObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/{repository}/{name}")
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @Order(6)
    void testThatAuthorizedUserCannotDownloadFromUnauthorizedRepository() {

        given()
            .auth().preemptive().oauth2(invalidUserAccessToken)
            .pathParam("repository", this.containerName)
            .pathParam("name", this.binaryObjectName)
            .basePath(OBJECTS_URL)
        .when()
            .get("/download/{repository}/{name}")
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @AfterAll
    void teardown() {
        LOG.infof("Teardown - deleting test blob container '%s'", this.containerName);
        given().port(8081)
            .auth().preemptive().oauth2(testAdminAccessToken)
            .basePath(REPOSITORIES_URL)
            .pathParam("name", this.containerName)
        .when().delete("/{name}").thenReturn();
    }

}