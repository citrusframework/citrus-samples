package org.citrusframework.samples.quarkus.camel.aws.s3.kafka;

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
import org.citrusframework.api.actions.testcontainers.aws2.AwsService;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.quarkus.LocalStackContainerSupport;
import org.citrusframework.testcontainers.kafka.quarkus.apache.KafkaContainerSupport;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.kafka.KafkaContainer;
import software.amazon.awssdk.services.s3.S3Client;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.aws2S3;

@QuarkusTest
@CitrusSupport
@LocalStackContainerSupport(services = AwsService.S3,
        containerLifecycleListener = QuarkusApplicationTest.LocalStackConfigurer.class)
@KafkaContainerSupport(port = 9092, version = "4.2.0",
        containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {

    private static final Logger log = LoggerFactory.getLogger(QuarkusApplicationTest.class);

    private static final String BUCKET_NAME = "citrus-camel-demo";

    @CitrusResource
    LocalStackContainer localStackContainer;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "s3-events")
    KafkaEndpoint s3Events;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldSplitS3FileToKafkaEvents() {
        runner.given(
            camel().bind("s3Client", localStackContainer.getClient(AwsService.S3))
        );

        runner.when(
            camel()
                .send()
                .endpoint(aws2S3(BUCKET_NAME)
                        .advanced()
                        .amazonS3Client("#s3Client")::getRawUri)
                .message()
                .fork(true)
                .body("Hello Camel!\nHello Citrus!\nHello Quarkus!")
                .header("CamelAwsS3Key", "hello.txt")
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Camel!" }
                    """)
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Citrus!" }
                    """)
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Quarkus!" }
                    """)
        );
    }

    public static class LocalStackConfigurer implements ContainerLifecycleListener<LocalStackContainer> {
        @Override
        public Map<String, String> started(LocalStackContainer container) {
            String serviceEndpoint = container.getServiceEndpoint().toString();
            log.info("LocalStack started at: {}", serviceEndpoint);

            S3Client s3Client = container.getClient(AwsService.S3);
            s3Client.createBucket(builder -> builder.bucket(BUCKET_NAME));
            log.info("Successfully created S3 bucket: {}", BUCKET_NAME);

            return Map.of(
                    "camel.kamelet.aws-s3-source.bucketNameOrArn", BUCKET_NAME,
                    "camel.kamelet.aws-s3-source.uriEndpointOverride", serviceEndpoint,
                    "camel.kamelet.aws-s3-source.accessKey", container.getAccessKey(),
                    "camel.kamelet.aws-s3-source.secretKey", container.getSecretKey(),
                    "camel.kamelet.aws-s3-source.region", container.getRegion(),
                    "camel.kamelet.aws-s3-source.overrideEndpoint", "true",
                    "camel.kamelet.aws-s3-source.forcePathStyle", "true"
            );
        }
    }

    public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
        @Override
        public Map<String, String> started(KafkaContainer container) {
            try (Admin adminClient = Admin.create(Collections.singletonMap(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers()))) {
                CreateTopicsResult result = adminClient.createTopics(Set.of(
                    new NewTopic("s3-events", 1, (short) 1)
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
