package org.citrusframework.samples.quarkus.camel.kafka;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.config.annotation.KafkaEndpointConfig;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.testcontainers.kafka.quarkus.apache.KafkaContainerSupport;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.kafka.KafkaContainer;

@QuarkusTest
@CitrusSupport
@KafkaContainerSupport(port = 9092, version = "4.2.0", containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {

    private static final Logger log = LoggerFactory.getLogger(QuarkusApplicationTest.class);

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-in")
    KafkaEndpoint wordsIn;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-out")
    KafkaEndpoint wordsOut;

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

    public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
        @Override
        public Map<String, String> started(KafkaContainer container) {
            try (Admin adminClient = Admin.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers()))) {
                CreateTopicsResult result = adminClient.createTopics(Set.of(
                    new NewTopic("words-in", 1, (short) 1),
                    new NewTopic("words-out", 1, (short) 1)
                ));

                result.all().get();

                adminClient.listTopics().names().get()
                        .forEach(topic -> log.info("Successfully created topic: {}", topic));
            } catch (ExecutionException | InterruptedException e) {
                throw new CitrusRuntimeException("Failed to create topics", e);
            }

            return Collections.emptyMap();
        }
    }
}
