package org.citrusframework.samples.quarkus.camel.mqtt;

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
import org.citrusframework.testcontainers.quarkus.GenericContainerProvider;
import org.citrusframework.testcontainers.quarkus.TestcontainersSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;

@QuarkusTest
@CitrusSupport
@TestcontainersSupport(containerProvider = QuarkusApplicationTest.MosquittoContainerProvider.class,
        containerLifecycleListener = QuarkusApplicationTest.MosquittoConfigurer.class)
@KafkaContainerSupport(port = 9092, version = "4.2.0", containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {

    private static final Logger log = LoggerFactory.getLogger(QuarkusApplicationTest.class);

    private static final int MOSQUITTO_PORT = 1883;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "temperature-warm",
            consumerGroup = "citrus-consumer-1")
    KafkaEndpoint temperatureWarm;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "temperature-cold",
            consumerGroup = "citrus-consumer-2")
    KafkaEndpoint temperatureCold;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldRouteWarmTemperature() {
        runner.when(
            camel()
                .send()
                .endpoint("paho-mqtt5:temperature?brokerUrl={{mqtt.broker.url}}")
                .message()
                .fork(true)
                .body("{\"value\": 25}")
        );

        runner.then(
            receive()
                .endpoint(temperatureWarm)
                .message()
                .body("25")
        );
    }

    @Test
    void shouldRouteColdTemperature() {
        runner.when(
            camel()
                .send()
                .endpoint("paho-mqtt5:temperature?brokerUrl={{mqtt.broker.url}}")
                .message()
                .fork(true)
                .body("{\"value\": 15}")
        );

        runner.then(
            receive()
                .endpoint(temperatureCold)
                .message()
                .body("15")
        );
    }

    public static class MosquittoContainerProvider implements GenericContainerProvider {
        @Override
        public GenericContainer<?> create() {
            return new GenericContainer<>("eclipse-mosquitto:latest")
                    .withExposedPorts(MOSQUITTO_PORT)
                    .withCommand("mosquitto", "-c", "/mosquitto-no-auth.conf", "-p", String.valueOf(MOSQUITTO_PORT))
                    .waitingFor(Wait.forListeningPort());
        }
    }

    public static class MosquittoConfigurer implements ContainerLifecycleListener<GenericContainer<?>> {
        @Override
        public Map<String, String> started(GenericContainer<?> container) {
            String brokerUrl = "tcp://%s:%d".formatted(container.getHost(), container.getMappedPort(MOSQUITTO_PORT));
            log.info("Mosquitto broker started at: {}", brokerUrl);
            return Map.of("mqtt.broker.url", brokerUrl);
        }
    }

    public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
        @Override
        public Map<String, String> started(KafkaContainer container) {
            try (Admin adminClient = Admin.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers()))) {
                CreateTopicsResult result = adminClient.createTopics(Set.of(
                    new NewTopic("temperature-warm", 1, (short) 1),
                    new NewTopic("temperature-cold", 1, (short) 1)
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
