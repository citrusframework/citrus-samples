package org.citrusframework.samples.quarkus.camel.rest.dsl;

import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.citrusframework.json.dsl.JsonSupport.marshal;

@QuarkusTest
@CitrusSupport
class FruitPojoRestDslTest implements TestActionSupport {

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    @Test
    void shouldAddFruit() {
        runner.when(
                http()
                        .client(fruitRestClient)
                        .send()
                        .post()
                        .path("/fruits")
                        .message()
                        .body(marshal(new Fruit("Pineapple", "A sweet and tasty tropical fruit.")))
                        .contentType(APPLICATION_JSON_CONTENT_TYPE)
        );

        runner.then(
                http()
                        .client(fruitRestClient)
                        .receive()
                        .response(201)
        );
    }

    @Test
    void shouldListFruits() {
        runner.when(
                http()
                        .client(fruitRestClient)
                        .send()
                        .get()
                        .path("/fruits")
        );

        runner.then(
                http()
                        .client(fruitRestClient)
                        .receive()
                        .response(200)
                        .message()
                        .validate(new JsonMappingValidationProcessor<>(Fruit[].class) {
                            @Override
                            public void validate(Fruit[] fruits, Map<String, Object> headers, TestContext context) {
                                Assertions.assertTrue(fruits.length > 0);
                                Assertions.assertTrue(Arrays.stream(fruits).anyMatch(fruit -> fruit.getName().equals("Apple")));
                            }
                        })
        );
    }

    @Test
    void shouldDeleteFruit() {
        runner.when(
                http().client(fruitRestClient)
                        .send()
                        .delete("/fruits")
                        .message()
                        .contentType(APPLICATION_JSON_CONTENT_TYPE)
                        .body(marshal(new Fruit("Apple", "")))
        );

        runner.then(
                http().client(fruitRestClient)
                        .receive()
                        .response(204)
        );
    }

    @Test
    void shouldHandleFruitNotFound() {
        runner.when(
                http().client(fruitRestClient)
                        .send()
                        .delete("/fruits")
                        .message()
                        .contentType(APPLICATION_JSON_CONTENT_TYPE)
                        .body(marshal(new Fruit("Pineapple", "A sweet and tasty tropical fruit.")))
        );

        runner.then(
                http().client(fruitRestClient)
                        .receive()
                        .response(404)
        );
    }
}
