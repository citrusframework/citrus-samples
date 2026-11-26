package org.citrusframework.samples.quarkus.camel.openapi.server;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

@QuarkusTest
@CitrusSupport
class PetstoreOpenApiTest {

    @CitrusResource
    TestCaseRunner t;

    OpenApiSpecification petstoreApi = OpenApiSpecification.from("http://localhost:8081/openapi");

    @Test
    void shouldGetPetById() {
        t.given(
            createVariables()
                .variable("petId", "1000")
        );

        t.when(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .send("getPetById")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .receive("getPetById", HttpStatus.OK)
        );
    }

    @Test
    void shouldAddPet() {
        t.when(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .send("addPet")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .receive("addPet", HttpStatus.OK)
        );
    }

    @Test
    void shouldUpdatePet() {
        t.when(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .send("updatePet")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .receive("updatePet", HttpStatus.OK)
        );
    }

    @Test
    void shouldDeletePet() {
        t.given(
            createVariables()
                .variable("petId", "1000")
        );

        t.when(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .send("deletePet")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8081")
                    .receive("deletePet", HttpStatus.OK)
        );
    }
}
