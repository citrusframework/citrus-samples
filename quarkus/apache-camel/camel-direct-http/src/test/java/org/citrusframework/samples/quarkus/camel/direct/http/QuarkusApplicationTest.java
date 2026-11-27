package org.citrusframework.samples.quarkus.camel.direct.http;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.config.annotation.HttpServerConfig;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = true, port = 9001)
    HttpServer translateServer;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldHandleEvents() {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:words-out");
        mockEndpoint.expectedBodiesReceived(">> HOWDY");

        runner.when(
            send()
                .fork(true)
                .endpoint("camel:direct:words-in")
                .message()
                .header("lang", "us-texas")
                .body("Hello")
        );

        runner.when(
            http().server(translateServer)
                .receive()
                .post("/translate")
                .message()
                .queryParam("lang", "us-texas")
                .body("Hello")
        );

        runner.then(
            http().server(translateServer)
                    .send()
                    .response(200)
                    .message()
                    .body("Howdy")
        );

        runner.then(
            context -> {
                try {
                    mockEndpoint.assertIsSatisfied();
                } catch (InterruptedException e) {
                    throw new CitrusRuntimeException("Failed to verify mock endpoint", e);
                }
            }
        );
    }
}
