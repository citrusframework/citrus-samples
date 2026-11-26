package org.citrusframework.samples.quarkus.camel.knative;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.api.actions.testcontainers.aws2.AwsService;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.api.kubernetes.ClusterType;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.quarkus.LocalStackContainerSupport;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

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

    @Test
    void shouldProduceCloudEvents() {
        runner.given(
            knative()
                .brokers()
                .create("default")
                .clusterType(ClusterType.LOCAL)
        );

        runner.when(this::uploadS3File);

        runner.then(
            knative()
                .event()
                .receive()
                .serviceName("default")
                .eventData(s3Data)
                .attribute("ce-id", "@matches([0-9A-Z]{15}-[0-9]{16})@")
                .attribute("ce-type", "dev.knative.eventing.aws-s3")
                .attribute("ce-source", "dev.knative.eventing.aws-s3-source")
                .attribute("ce-subject", "aws-s3-source")
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

        return Map.of(
            "camel.kamelet.aws-s3-source.accessKey", container.getAccessKey(),
            "camel.kamelet.aws-s3-source.secretKey", container.getSecretKey(),
            "camel.kamelet.aws-s3-source.region", container.getRegion(),
            "camel.kamelet.aws-s3-source.bucketNameOrArn", s3BucketName,
            "camel.kamelet.aws-s3-source.uriEndpointOverride", container.getServiceEndpoint().toString(),
            "camel.kamelet.aws-s3-source.overrideEndpoint", "true",
            "camel.kamelet.aws-s3-source.forcePathStyle", "true"
        );
    }
}
