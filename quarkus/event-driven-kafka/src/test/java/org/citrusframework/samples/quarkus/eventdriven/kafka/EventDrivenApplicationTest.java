package org.citrusframework.samples.quarkus.eventdriven.kafka;

import io.quarkus.test.junit.QuarkusTest;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kafka.config.annotation.KafkaEndpointConfig;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class EventDrivenApplicationTest implements TestActionSupport {

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-in",
            server = "${kafka.bootstrap.servers}")
    KafkaEndpoint wordsIn;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-out",
            server = "${kafka.bootstrap.servers}")
    KafkaEndpoint wordsOut;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldHandleEvents() {
        runner.when(
            send()
                .endpoint(wordsIn)
                .message()
                .body("Hi")
        );

        runner.then(
            receive()
                .endpoint(wordsOut)
                .message()
                .body(">> HI")
        );
    }
}
