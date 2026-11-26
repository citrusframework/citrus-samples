package org.citrusframework.samples.quarkus.camel.direct;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

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
