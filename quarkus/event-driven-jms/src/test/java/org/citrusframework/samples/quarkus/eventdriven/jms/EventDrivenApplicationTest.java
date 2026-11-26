package org.citrusframework.samples.quarkus.eventdriven.jms;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.jms.config.annotation.JmsEndpointConfig;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
@WithTestResource(ArtemisTestResource.class)
class EventDrivenApplicationTest implements TestActionSupport {

    @Inject
    @BindToRegistry
    ConnectionFactory connectionFactory;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName = "words-in")
    JmsEndpoint wordsIn;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName = "words-out")
    JmsEndpoint wordsOut;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldHandleEvents() {
        runner.when(
            send()
                .endpoint(wordsIn)
                .message()
                .body("Howdy")
        );

        runner.then(
            receive()
                .endpoint(wordsOut)
                .message()
                .body(">> HOWDY")
        );
    }
}
