package dev.orion.moodle;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceTest {

    @Test
    @DisplayName("Test empty input")
    @Order(1)
    public void empty() {
        given()
                .formParam("githubProfileURL", "")
                .formParam("moodleProfileURL", "")
                .formParam("moodleAssign", "")
                .when().post("/verify")
                .then()
                .statusCode(400);
        // .body(is("Rodrigo Prestes Machado"));
    }

    @Test
    @DisplayName("Test wrong users")
    @Order(2)
    public void wrong() {
        given()
                .formParam("githubProfileURL", "https://github.com")
                .formParam("moodleProfileURL", "https://moodle.poa.ifrs.edu.br")
                .formParam("moodleAssign", "https://moodle.poa.ifrs.edu.br")
                .when().post("/verify")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Test wrong moodle")
    @Order(3)
    public void wrongMoodle() {
        given()
                .formParam("githubProfileURL", "https://github.com/rodrigoprestesmachado")
                .formParam("moodleProfileURL", "https://moodle.poa.ifrs.edu.br")
                .formParam("moodleAssign", "https://moodle.poa.ifrs.edu.br")
                .when().post("/verify")
                .then()
                .statusCode(400);
    }

}