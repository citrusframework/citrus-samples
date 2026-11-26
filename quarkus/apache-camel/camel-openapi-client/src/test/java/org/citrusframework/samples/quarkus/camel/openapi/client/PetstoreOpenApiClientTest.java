package org.citrusframework.samples.quarkus.camel.openapi.client;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.citrusframework.json.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = CitrusEndpointConfig.class)
class PetstoreOpenApiClientTest implements TestActionSupport {

    @CitrusEndpoint
    HttpServer petstoreServer;

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @BindToRegistry
    ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    @CitrusResource
    GherkinTestActionRunner runner;

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
            http()
                .server(petstoreServer)
                .receive()
                .get("/pet/${petId}")
                .message()
        );

        runner.then(
            http()
                    .server(petstoreServer)
                    .send()
                    .response(HttpStatus.OK)
                    .message()
                    .body(new DefaultPayloadBuilder(newPet(1000, "hasso", "dog", 1L, "available")))
                    .process(processor().camel().marshal().json())
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
            http()
                    .server(petstoreServer)
                    .receive()
                    .post("/pet")
                    .message()
                    .body(new ObjectMappingPayloadBuilder(petBody))
                    .contentType("application/json;charset=UTF-8")
        );

        runner.then(
            http()
                    .server(petstoreServer)
                    .send()
                    .response(HttpStatus.CREATED)
        );
    }

    @Test
    void shouldUpdatePet() {
        Map<String, Object> petBody = newPet(1001L, "fluffy", "cat", 2L, "sold");

        runner.when(
            camel().send()
                    .endpoint(CamelSupport.camel().endpoint(direct("updatePet")::getRawUri))
                    .fork(true)
                    .message()
                    .body(new DefaultPayloadBuilder(petBody))
                    .header(Exchange.CONTENT_TYPE, "application/json")
        );

        runner.then(
            http()
                    .server(petstoreServer)
                    .receive()
                    .put("/pet")
                    .message()
                    .body(new ObjectMappingPayloadBuilder(petBody))
                    .contentType("application/json;charset=UTF-8")
        );

        runner.then(
            http()
                    .server(petstoreServer)
                    .send()
                    .response(HttpStatus.NO_CONTENT)
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
            http()
                    .server(petstoreServer)
                    .receive()
                    .delete("/pet/${petId}")
        );

        runner.then(
            http()
                    .server(petstoreServer)
                    .send()
                    .response(HttpStatus.NO_CONTENT)
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
