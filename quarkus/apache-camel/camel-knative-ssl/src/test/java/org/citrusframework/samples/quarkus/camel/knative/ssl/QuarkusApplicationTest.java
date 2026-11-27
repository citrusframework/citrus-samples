package org.citrusframework.samples.quarkus.camel.knative.ssl;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.api.actions.testcontainers.aws2.AwsService;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.endpoint.builder.HttpEndpoints;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.quarkus.LocalStackContainerSupport;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

@QuarkusTest
@CitrusSupport
@LocalStackContainerSupport(services = AwsService.S3, containerLifecycleListener = QuarkusApplicationTest.class)
public class QuarkusApplicationTest implements TestActionSupport, ContainerLifecycleListener<LocalStackContainer> {

    private static final Logger log = LoggerFactory.getLogger(QuarkusApplicationTest.class);

    @CitrusResource
    GherkinTestActionRunner runner;

    final String s3Key = "message.txt";
    final String s3Data = "Hello Knative!";
    final String s3BucketName = "knative-bucket";

    @CitrusResource
    private LocalStackContainer localStackContainer;

    @BindToRegistry
    public HttpServer knativeBroker = HttpEndpoints.http()
            .server()
            .port(8080)
            .timeout(5000L)
            .securePort(8443)
            .secured(HttpSecureConnection.ssl()
                    .keyStore("classpath:keystore/server.jks", "secr3t")
                    .trustStore("classpath:keystore/truststore.jks", "secr3t"))
            .autoStart(true)
            .build();

    @Test
    void shouldProduceCloudEventsOverSsl() {
        runner.when(this::uploadS3File);

        runner.then(
            http().server(knativeBroker)
                    .receive()
                    .post()
                    .message()
                    .body(s3Data)
                    .header("ce-id", "@matches([0-9A-Z]{15}-[0-9]{16})@")
                    .header("ce-type", "dev.knative.eventing.aws-s3")
                    .header("ce-source", "dev.knative.eventing.aws-s3-source")
                    .header("ce-subject", "aws-s3-source")
        );

        runner.then(
            http().server(knativeBroker)
                    .send()
                    .response(HttpStatus.OK)
        );
    }

    private void uploadS3File(TestContext context) {
        S3Client s3Client = localStackContainer.getClient(AwsService.S3);

        CreateMultipartUploadResponse initResponse = s3Client.createMultipartUpload(b -> b.bucket(s3BucketName).key(s3Key));
        String etag = s3Client.uploadPart(b -> b.bucket(s3BucketName)
                        .key(s3Key)
                        .uploadId(initResponse.uploadId())
                        .partNumber(1),
                RequestBody.fromString(s3Data)).eTag();
        s3Client.completeMultipartUpload(b -> b.bucket(s3BucketName)
                .multipartUpload(CompletedMultipartUpload.builder()
                        .parts(Collections.singletonList(CompletedPart.builder()
                                .partNumber(1)
                                .eTag(etag).build())).build())
                .key(s3Key)
                .uploadId(initResponse.uploadId()));
    }

    @Override
    public Map<String, String> started(LocalStackContainer container) {
        String serviceEndpoint = container.getServiceEndpoint().toString();
        log.info("LocalStack started at: {}", serviceEndpoint);

        S3Client s3Client = container.getClient(AwsService.S3);
        s3Client.createBucket(builder -> builder.bucket(s3BucketName));
        log.info("Successfully created S3 bucket: {}", s3BucketName);

        return Map.ofEntries(
            Map.entry("camel.kamelet.aws-s3-source.accessKey", container.getAccessKey()),
            Map.entry("camel.kamelet.aws-s3-source.secretKey", container.getSecretKey()),
            Map.entry("camel.kamelet.aws-s3-source.region", container.getRegion()),
            Map.entry("camel.kamelet.aws-s3-source.bucketNameOrArn", s3BucketName),
            Map.entry("camel.kamelet.aws-s3-source.uriEndpointOverride", container.getServiceEndpoint().toString()),
            Map.entry("camel.kamelet.aws-s3-source.overrideEndpoint", "true"),
            Map.entry("camel.kamelet.aws-s3-source.forcePathStyle", "true"),
            Map.entry("camel.knative.client.ssl.enabled", "true"),
            Map.entry("camel.knative.client.ssl.verify.hostname", "false"),
            Map.entry("camel.knative.client.ssl.key.path", "keystore/client.pem"),
            Map.entry("camel.knative.client.ssl.key.cert.path", "keystore/client.crt"),
            Map.entry("camel.knative.client.ssl.truststore.path", "keystore/truststore.jks"),
            Map.entry("camel.knative.client.ssl.truststore.password", "secr3t")
        );
    }
}
