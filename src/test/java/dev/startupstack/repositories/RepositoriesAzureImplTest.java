package dev.startupstack.repositories;

import static dev.startupstack.storageservice.Constants.REPOSITORIES_URL;
import static dev.startupstack.TestUtils.testAdminAccessToken;
import static dev.startupstack.TestUtils.testUserAccessToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import dev.startupstack.TestUtils;
import dev.startupstack.storageservice.repostitories.models.RepositoriesModel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class RepositoriesAzureImplTest {

    String containerName = "testcontainer";
    String tenantID = "mytenantid";

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.keycloak.url")
    String keycloakURL;

    @BeforeAll
    void initialize() {
        TestUtils.generateAccessTokens(keycloakURL);
    }

    
    @Test
    @Order(1)
    void testThatAdminCanCreateRepository() {
        RepositoriesModel model = new RepositoriesModel(this.containerName);
        
        given()
            .auth().preemptive().oauth2(testAdminAccessToken)
            .body(model).contentType(ContentType.JSON)
        .when().post(REPOSITORIES_URL)
        .then()
            .statusCode(Status.CREATED.getStatusCode());

    }

    @Test
    void testThatRepositoryInfoCanBeRetrieved() {
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .basePath(REPOSITORIES_URL)
            .pathParam("name", "containerretrievaltest")
        .when().get("/{name}")
        .then()
            .statusCode(Status.OK.getStatusCode())
            .body("name", equalTo("containerretrievaltest")).and()
            .body("tenantID", equalTo(this.tenantID));

    }
    @Test
    void testThatUserCannotCreateRepository() {
        RepositoriesModel model = new RepositoriesModel(this.containerName);
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .body(model).contentType(ContentType.JSON)
        .when().post(REPOSITORIES_URL)
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void testThatUserCannotDeleteRepository() {
        given()
            .auth().preemptive().oauth2(testUserAccessToken)
            .basePath(REPOSITORIES_URL)
            .pathParam("name", this.containerName)
        .when().delete("/{name}")
        .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }
    @Test
    void testThatUnauthorizedUserCannotAccessAnyResource() {
        RepositoriesModel model = new RepositoriesModel(this.containerName);
        given()
            .basePath(REPOSITORIES_URL)
            .pathParam("name", this.containerName)
        .when().delete("/{name}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());

        given()
            .body(model).contentType(ContentType.JSON)
        .when().post(REPOSITORIES_URL)
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());

        given()
            .basePath(REPOSITORIES_URL)
            .pathParam("name", "containerretrievaltest")
        .when().get("/{name}")
        .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }
    
    @Test
    @Order(99)
    void testThatRepositoryCanBeDeleted() {
        given()
            .auth().preemptive().oauth2(testAdminAccessToken)
            .basePath(REPOSITORIES_URL)
            .pathParam("name", this.containerName)
        .when().delete("/{name}")
        .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }
        
}