# Apache Camel AWS S3 to Kafka Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes in Quarkus applications using the Citrus framework with AWS S3 and Kafka integration. The project showcases a Camel route that consumes files from an AWS S3 bucket via the `aws-s3-source` Kamelet, splits each file into individual lines, wraps each line as a JSON event, and produces the resulting events onto a Kafka topic.

## Why Citrus for AWS S3 Testing?

Citrus provides built-in support for **LocalStack Testcontainers** through the `@LocalStackContainerSupport` annotation. This makes it straightforward to:

- Spin up a LocalStack container simulating AWS S3 locally
- Inject dynamic connection properties into the Quarkus application context
- Use Apache Camel's `aws2-s3` component to upload test files through Citrus's `CamelSupport.camel()` DSL

For Kafka verification, Citrus provides the `@KafkaContainerSupport` annotation and `@KafkaEndpointConfig` - a simple, declarative way to provision Kafka broker infrastructure and configure test endpoints:

```java
@KafkaContainerSupport(port = 9092, version = "4.2.0")
class QuarkusApplicationTest {
    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "s3-events")
    KafkaEndpoint s3Events;
}
```

## What You'll Learn

By the end of this guide, you'll understand:

- **Camel S3 via Kamelet**: How to consume S3 files using the `aws-s3-source` Kamelet with LocalStack
- **Splitter EIP**: How to split file content into individual lines and process each as a separate message
- **JSON Transformation**: How to wrap each split line into a JSON event using Simple expressions
- **Kamelet Property Binding**: How to configure Kamelet properties via `camel.kamelet.*` in `application.properties`
- **Test-Scoped Configuration**: How to keep test-only settings out of production `application.properties`
- **LocalStack Testcontainers**: How to use `@LocalStackContainerSupport` to start a local S3 service
- **Citrus + Camel S3 Testing**: How to use Camel's `aws2-s3` component through Citrus for uploading files in tests
- **Multi-Protocol Testing**: How to combine S3 (upload) and Kafka (receive/verify) in a single integration test

## The Application Under Test

The Quarkus application uses Apache Camel to implement an integration route that bridges AWS S3 and Kafka:

```
S3 Bucket (citrus-camel-demo) → Split by newline → Filter empty lines → JSON wrap → Kafka Topic (s3-events)
```

### Apache Camel Route

The application consists of a single Camel route defined in `Routes.java`:

```java
public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:aws-s3-source")
            .split(body().tokenize("\n"))
            .filter(simple("${body} != \"\""))
            .setBody()
                .simple("""
                    { "message": "${body}" }
                    """)
            .to(kafka("s3-events"));
    }
}
```

The Kamelet URI no longer carries any inline connection parameters. All `aws-s3-source` Kamelet properties are bound via the standard Camel Kamelet property binding mechanism in `application.properties` (see below).

**Route Breakdown:**

1. **from("kamelet:aws-s3-source")**: Consumes files from the S3 bucket using the `aws-s3-source` Kamelet — properties are resolved through Camel's Kamelet property binding (`camel.kamelet.aws-s3-source.*`)
2. **.split(body().tokenize("\n"))**: Splits the file content by newline, processing each line as an individual message
3. **.filter(simple("${body} != \"\""))**: Filters out empty lines
4. **.setBody().simple(...)**: Wraps each line in a JSON structure `{ "message": "..." }`
5. **.to(kafka("s3-events"))**: Produces each JSON event to the `s3-events` Kafka topic

### Application Configuration

`src/main/resources/application.properties` holds the production defaults for both the Kamelet and Kafka:

```properties
# Kamelet source properties
camel.kamelet.aws-s3-source.bucketNameOrArn=citrus-camel-demo
camel.kamelet.aws-s3-source.region=us-east-1
camel.kamelet.aws-s3-source.accessKey=accesskey
camel.kamelet.aws-s3-source.secretKey=secretkey

# Kafka endpoint
kafka.bootstrap.servers=my-cluster.namespace.local
%test.kafka.bootstrap.servers=localhost:9092
```

The `%test.kafka.bootstrap.servers` entry is a Quarkus profile-scoped override that applies only in the `test` profile. This keeps the production value clean while still wiring the Kafka client to the Testcontainer-managed broker during tests.

Note that test-specific Kamelet settings such as `overrideEndpoint`, `forcePathStyle`, and `uriEndpointOverride` are **not** present here — they are injected dynamically by the `LocalStackConfigurer` lifecycle listener (see below).

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to verify the end-to-end routing behavior using Citrus framework.

### Test Setup: LocalStack for AWS S3

The test uses `@LocalStackContainerSupport` to start a LocalStack container with the S3 service enabled:

```java
@LocalStackContainerSupport(services = AwsService.S3,
        containerLifecycleListener = QuarkusApplicationTest.LocalStackConfigurer.class)
class QuarkusApplicationTest {

    public static class LocalStackConfigurer implements ContainerLifecycleListener<LocalStackContainer> {
        @Override
        public Map<String, String> started(LocalStackContainer container) {
            // Create the S3 bucket
            S3Client s3Client = container.getClient(AwsService.S3);
            s3Client.createBucket(builder -> builder.bucket(BUCKET_NAME));

            String serviceEndpoint = container.getServiceEndpoint().toString();

            // Return Kamelet properties — overrides the production defaults and adds
            // test-only settings (overrideEndpoint, forcePathStyle, uriEndpointOverride)
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
}
```

**What's happening here:**

- `@LocalStackContainerSupport(services = AwsService.S3)`: Starts a LocalStack container with only the S3 service
- `LocalStackConfigurer`: Creates the test bucket and returns dynamic `camel.kamelet.aws-s3-source.*` properties that are injected into the Quarkus application context, overriding the production defaults and supplying the LocalStack-specific settings (`overrideEndpoint`, `forcePathStyle`, `uriEndpointOverride`) that must not appear in the production `application.properties`

### Uploading Files via Camel's S3 Component

The test uses Camel's `aws2-s3` component through `TestActionSupport.camel()` to upload a multi-line file. Instead of building a raw URI string, it uses the type-safe `aws2S3()` endpoint DSL and binds the `S3Client` bean from the LocalStack container:

```java
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
```

**What's happening here:**

- `camel().bind("s3Client", ...)`: Binds the `S3Client` from the LocalStack container into the Camel registry under the name `s3Client`
- `aws2S3(BUCKET_NAME).advanced().amazonS3Client("#s3Client")`: Uses the type-safe endpoint DSL and references the bound client via `#s3Client`, removing the need for a manually constructed URI with connection parameters
- `.header("CamelAwsS3Key", "hello.txt")`: Sets the S3 object key for the uploaded file
- `.fork(true)`: Sends the message asynchronously so the test can proceed to verify the Kafka output

### Verifying Kafka Events

After the S3 file is processed by the Camel route, the test verifies that each line produces a separate JSON event on the Kafka topic:

```java
@CitrusEndpoint
@KafkaEndpointConfig(topic = "s3-events", consumerGroup = "citrus-consumer-1")
KafkaEndpoint s3Events;

runner.then(
    receive()
        .endpoint(s3Events)
        .message()
        .body("{ \"message\": \"Hello Camel!\" }")
);
```

### Test Execution Flow

1. **WHEN**: Upload a file with 3 lines (`Hello Camel!\nHello Citrus!\nHello Quarkus!`) to the S3 bucket
2. The `aws-s3-source` Kamelet polls the bucket and picks up the file
3. The Splitter EIP splits the file content into 3 individual messages
4. Each line is wrapped as JSON: `{ "message": "Hello Camel!" }`
5. **THEN**: Verify that 3 JSON events arrive on the `s3-events` Kafka topic

## Key Testing Concepts

### 1. Kamelet Property Binding

The `aws-s3-source` Kamelet is configured exclusively through the `camel.kamelet.aws-s3-source.*` namespace in `application.properties`. This is the idiomatic Camel Kamelet approach and keeps the route URI clean. Production defaults (bucket name, region, credentials) are declared in `src/main/resources/application.properties`; test-specific overrides and LocalStack-only settings are injected at runtime by the `LocalStackConfigurer`.

### 2. Separation of Production and Test Configuration

Test-scoped settings that only make sense against a local container (`overrideEndpoint`, `forcePathStyle`, `uriEndpointOverride`) are never written into the main `application.properties`. They are returned by the `ContainerLifecycleListener` and injected dynamically, so the production configuration remains unaffected.

The Kafka bootstrap address follows the same pattern: `kafka.bootstrap.servers` holds the real cluster address; `%test.kafka.bootstrap.servers=localhost:9092` is a Quarkus profile override that activates only in the `test` profile.

### 3. LocalStack Container Support

The `@LocalStackContainerSupport` annotation starts a LocalStack container with specific AWS services. The `ContainerLifecycleListener` pattern allows you to perform setup (like creating buckets) and inject dynamic connection properties into the Quarkus application context.

### 4. Citrus Camel Integration for S3

The test binds the `S3Client` from the LocalStack container into the Camel registry and uses the type-safe `aws2S3()` endpoint DSL to upload files. This avoids hand-crafting URI strings with connection parameters and reuses the infrastructure already provided by the container.

### 5. Forked Message Sending

The `.fork(true)` option is important because the S3 upload and subsequent Kamelet polling happen asynchronously. It ensures the test thread continues to the verification step while the file is being uploaded and processed.

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-aws-s3-kafka/pom.xml clean test
```

**What happens during test execution:**

1. `@LocalStackContainerSupport` starts a LocalStack container with the S3 service
2. `@KafkaContainerSupport` starts a Kafka broker container
3. The `LocalStackConfigurer` creates the S3 bucket and injects `camel.kamelet.aws-s3-source.*` connection properties (including `overrideEndpoint` and `forcePathStyle`) into the application context
4. The `KafkaConfigurer` creates the `s3-events` topic
5. Quarkus starts the application with the Camel route connecting to both services
6. The test uploads a multi-line file to S3 and verifies the split events arrive on Kafka

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Apache Camel AWS S3 Component](https://camel.apache.org/components/latest/aws2-s3-component.html) - Camel S3 component reference
- [Apache Camel Splitter EIP](https://camel.apache.org/components/latest/eips/split-eip.html) - Splitter EIP
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/latest/) - Kamelet catalog
- [LocalStack](https://localstack.cloud/) - AWS cloud service emulator used in the test
