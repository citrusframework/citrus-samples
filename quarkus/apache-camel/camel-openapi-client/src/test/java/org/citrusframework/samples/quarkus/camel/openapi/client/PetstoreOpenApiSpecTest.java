package org.citrusframework.samples.quarkus.camel.openapi.client;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.dsl.CamelSupport;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = CitrusEndpointConfig.class)
class PetstoreOpenApiSpecTest implements TestActionSupport {

    @CitrusEndpoint
    HttpServer petstoreServer;

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;

    OpenApiSpecification petstoreApi = OpenApiSpecification.from("classpath:petstore-api.json");

    @Test
    void shouldGetPetById() {
        runner.given(
            createVariables()
                .variable("petId", "1000")
        );

        runner.when(
            camel().send()
                    .endpoint(CamelSupport.camel().endpoint(direct("getPetById")::getRawUri))
                    .fork(true)
                    .message().header("petId", "${petId}")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .receive("getPetById")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .send("getPetById", HttpStatus.OK)
        );
    }

    @Test
    void shouldAddPet() {
        Map<String, Object> petBody = newPet(0L, "hasso", "dog", 1L, "available");

        runner.when(
            camel().send()
                    .endpoint(CamelSupport.camel().endpoint(direct("addPet")::getRawUri))
                    .fork(true)
                    .message()
                    .body(new DefaultPayloadBuilder(petBody))
                    .header(Exchange.CONTENT_TYPE, "application/json")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .receive("addPet")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .send("addPet", HttpStatus.CREATED)
        );
    }

    @Test
    void shouldUpdatePet() {
        Map<String, Object> petBody = newPet(1000L, "fluffy", "cat", 2L, "sold");

        runner.when(
            camel().send()
                    .endpoint(CamelSupport.camel().endpoint(direct("updatePet")::getRawUri))
                    .fork(true)
                    .message()
                    .body(new DefaultPayloadBuilder(petBody))
                    .header(Exchange.CONTENT_TYPE, "application/json")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .receive("updatePet")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .send("updatePet", HttpStatus.NO_CONTENT)
        );
    }

    @Test
    void shouldDeletePet() {
        runner.given(
            createVariables()
                .variable("petId", "1000")
        );

        runner.when(
            camel().send()
                    .endpoint(CamelSupport.camel().endpoint(direct("deletePet")::getRawUri))
                    .fork(true)
                    .message().header("petId", "${petId}")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .receive("deletePet")
        );

        runner.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .send("deletePet", HttpStatus.NO_CONTENT)
        );
    }

    private static Map<String, Object> newPet(long id, String name, String categoryName,
                                               long categoryId, String status) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", categoryId);
        category.put("name", categoryName);

        Map<String, Object> pet = new HashMap<>();
        pet.put("id", id);
        pet.put("name", name);
        pet.put("category", category);
        pet.put("status", status);
        return pet;
    }
}
