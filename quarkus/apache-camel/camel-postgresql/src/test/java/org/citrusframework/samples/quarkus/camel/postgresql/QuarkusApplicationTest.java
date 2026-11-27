package org.citrusframework.samples.quarkus.camel.postgresql;

import javax.sql.DataSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@QuarkusTest
@CitrusSupport(devServicesProperties = "*")
class QuarkusApplicationTest implements TestActionSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient httpClient;

    @Inject
    DataSource dataSource;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldPersistData() {
        runner.given(
            createVariables()
                .variable("id", "citrus:randomNumber(4)")
                .variable("headline", "Camel rocks!")
        );

        runner.when(
            http().client(httpClient)
                .send()
                .post("/headline")
                .message()
                .body("""
                    { "id": ${id}, "headline": "${headline}" }
                    """)
                .contentType("application/json")
        );

        runner.then(
            http().client(httpClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .body("Headline created!")
        );

        runner.then(
            sql().dataSource(dataSource)
                .query()
                .statement("SELECT headline FROM headlines WHERE id=${id}")
                .validate("HEADLINE", "${headline}")
        );
    }
}
