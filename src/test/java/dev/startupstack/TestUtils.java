package dev.startupstack;

import static io.restassured.RestAssured.given;
import static dev.startupstack.storageservice.Constants.METADATA_TENANT_ID;

import org.jboss.logging.Logger;

import io.restassured.response.Response;

/**
 * TestUtils
 */
public class TestUtils {

    private static final Logger LOG = Logger.getLogger(TestUtils.class);

    public static String testUserAccessToken = null;
    public static String testAdminAccessToken = null;
    public static String invalidUserAccessToken = null;

    public static String testUserTenantID = null;
    public static String testAdminTenantID = null;
    public static String invalidUserTenantID = null;

    
    public static void generateAccessTokens(String keycloakURL) {

        if (testUserAccessToken == null) {
            LOG.info("Generating new access token for testUser");

            Response userResponse =  
                given()
                    .baseUri(keycloakURL)
                    .auth().preemptive().basic("backend-service", "secret")
                    .param("grant_type", "password")
                    .param("username", "jdoe")
                    .param("password", "jdoe")
                .when()
                    .post(keycloakURL + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().response();

            testUserAccessToken = userResponse.jsonPath().getString("access_token");
            testUserTenantID = userResponse.jsonPath().getString(METADATA_TENANT_ID);

        } 
        if (testAdminAccessToken == null) {
            LOG.info("Generating new access token for adminUser");

            Response adminResponse = 
                given()
                    .baseUri(keycloakURL)
                    .auth().preemptive().basic("backend-service", "secret")
                    .param("grant_type", "password")
                    .param("username", "admin")
                    .param("password", "admin")
                .when()
                    .post(keycloakURL + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().response();

            testAdminAccessToken = adminResponse.jsonPath().getString("access_token");
            testAdminTenantID = adminResponse.jsonPath().getString(METADATA_TENANT_ID);
        }

        if (invalidUserAccessToken == null) {
            LOG.info("Generating new access token for invalid user");

            Response invalidUserResponse = 
                given()
                    .baseUri(keycloakURL)
                    .auth().preemptive().basic("backend-service", "secret")
                    .param("grant_type", "password")
                    .param("username", "alice")
                    .param("password", "alice")
                .when()
                    .post(keycloakURL + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().response();

            invalidUserAccessToken = invalidUserResponse.jsonPath().getString("access_token");
            invalidUserTenantID = invalidUserResponse.jsonPath().getString(METADATA_TENANT_ID);
        }



    }
}