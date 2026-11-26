package org.citrusframework.samples.quarkus.camel.rest.dsl;

import io.quarkus.test.junit.QuarkusTest;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class FruitRestDslTest implements TestActionSupport {

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldAddFruit() {
        runner.when(
                http()
                        .client(fruitRestClient)
                        .send()
                        .post()
                        .path("/fruits")
                        .message()
                        .body("""
                        {
                           "name": "Pineapple",
                           "description": "A sweet and tasty tropical fruit."
                        }
                        """)
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
                        .validate(validation().jsonPath()
                                .expression("$..name", "@contains(Apple,Mango,Orange)@"))
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
                        .body("""
                        {
                            "name": "Apple"
                        }
                        """)
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
                        .body("""
                        {
                            "name": "Pineapple",
                            "description": "A sweet and tasty tropical fruit."
                        }
                        """)
        );

        runner.then(
                http().client(fruitRestClient)
                        .receive()
                        .response(404)
        );
    }
}
