# Apache Camel Knative Eventing with Citrus

This example demonstrates how to test an Apache Camel Knative event source that turns Amazon S3 uploads into CloudEvents and sends them to a Knative broker, running locally with Citrus and LocalStack.

## What You'll Learn

By the end of this guide, you'll understand:

- **Local Knative broker testing**: How Citrus starts a lightweight local Knative broker so you can verify Knative event delivery without a Kubernetes cluster
- **S3 event simulation**: How LocalStack provides an S3-compatible service for exercising the Camel `aws-s3-source` Kamelet in tests
- **CloudEvent verification**: How to assert Knative CloudEvent attributes such as `ce-id`, `ce-type`, `ce-source`, and `ce-subject`
- **SinkBinding-style configuration**: How Camel Knative reads `knative.json` and resolves the broker sink URL from the `k.sink` property

## The Application Under Test

The Quarkus application uses Apache Camel to consume objects from an S3 bucket and publish them as CloudEvents to a Knative broker:

```
Amazon S3 upload → Camel aws-s3-source Kamelet → CloudEvent transform → Knative broker
```

### Apache Camel Route

The route is defined in [`Routes.java`](src/main/java/org/citrusframework/samples/quarkus/camel/knative/Routes.java):

```java
from("kamelet:aws-s3-source")
        .transformDataType(new DataType("http:application-cloudevents"))
        .to("knative:event/org.apache.camel.event.messages?kind=Broker&name=default");
```

**Route breakdown:**

1. **`kamelet:aws-s3-source`**: Polls an S3 bucket using the Camel AWS S3 source Kamelet.
2. **`transformDataType("http:application-cloudevents")`**: Converts the exchange into a CloudEvent HTTP message.
3. **`knative:event/...`**: Sends the CloudEvent to the Knative broker named `default`.

### Camel Knative Configuration

The Camel Knative component reads a JSON environment file configured in [`application.properties`](src/main/resources/application.properties):

```properties
camel.component.knative.environment-path=classpath:knative.json
camel.component.knative.ceOverride[ce-type]=dev.knative.eventing.aws-s3
camel.component.knative.ceOverride[ce-source]=dev.knative.eventing.aws-s3-source
camel.component.knative.ceOverride[ce-subject]=aws-s3-source
```

These `ceOverride` settings make the produced event look like a Knative AWS S3 source event by explicitly setting the CloudEvent metadata.

The [`knative.json`](src/main/resources/knative.json) file defines the broker sink:

```json
{
  "resources": [
    {
      "name": "default",
      "type": "event",
      "endpointKind": "sink",
      "url": "{{k.sink:http://localhost:8080}}",
      "objectApiVersion": "eventing.knative.dev/v1",
      "objectKind": "Broker",
      "objectName": "default"
    }
  ]
}
```

The broker URL is resolved from `k.sink`, defaulting to `http://localhost:8080`. In tests, [`application.properties`](src/main/resources/application.properties) sets `%test.k.sink=http://localhost:8080` so Camel sends events to the Citrus local broker.

### Knative Deployment Descriptor

The module now also includes a sample Knative deployment descriptor in [`kubernetes.yml`](src/main/kubernetes/kubernetes.yml). It contains:

- a `Deployment` named `aws-s3-source`
- a `SinkBinding` that injects the `default` broker sink into that deployment

This mirrors how a real Knative environment would provide the sink URL to the event source.

## Understanding the Citrus Test

The test class [`QuarkusApplicationTest`](src/test/java/org/citrusframework/samples/quarkus/camel/knative/QuarkusApplicationTest.java) verifies the end-to-end flow by combining a local Knative broker with a LocalStack-backed S3 bucket.

### Test Setup

```java
@QuarkusTest
@CitrusSupport
@LocalStackContainerSupport(services = AwsService.S3, containerLifecycleListener = QuarkusApplicationTest.class)
public class QuarkusApplicationTest implements TestActionSupport, ContainerLifecycleListener<LocalStackContainer> {
```

Key pieces:

- `@QuarkusTest`: starts the Quarkus application and Camel route
- `@CitrusSupport`: integrates Citrus with the test lifecycle
- `@LocalStackContainerSupport(...)`: starts a LocalStack container with S3 enabled
- `ContainerLifecycleListener<LocalStackContainer>`: injects dynamic S3 connection properties into the Camel Kamelet configuration when the container starts

### Creating a Local Knative Broker

```java
runner.given(
    knative()
        .brokers()
        .create("default")
        .clusterType(ClusterType.LOCAL)
);
```

`ClusterType.LOCAL` tells Citrus to create a local HTTP broker endpoint instead of calling the Kubernetes API.

### Triggering the Event Source

The test uploads a file into LocalStack S3:

```java
runner.when(this::uploadS3File);
```

Inside [`uploadS3File()`](src/test/java/org/citrusframework/samples/quarkus/camel/knative/QuarkusApplicationTest.java#L67), the test performs a multipart upload to the bucket `knative-bucket` with the content `Hello Knative!`. The Camel `aws-s3-source` Kamelet consumes that object and forwards it as a CloudEvent.

When LocalStack starts, the test also creates the bucket and returns the Camel Kamelet properties needed to connect to the emulated S3 endpoint from [`started()`](src/test/java/org/citrusframework/samples/quarkus/camel/knative/QuarkusApplicationTest.java#L86).

### Receiving and Verifying CloudEvents

```java
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
```

The `receive()` action validates:

- **`eventData`**: the uploaded file content
- **`ce-id`**: a generated identifier matching the AWS S3 source event format used here
- **`ce-type`**: `dev.knative.eventing.aws-s3`
- **`ce-source`**: `dev.knative.eventing.aws-s3-source`
- **`ce-subject`**: `aws-s3-source`

### Test Flow

```
1. Citrus starts a local Knative broker named default on localhost:8080
2. LocalStack starts an S3 service and the test creates bucket knative-bucket
3. Camel aws-s3-source connects to LocalStack using the injected test properties
4. The test uploads message.txt with content "Hello Knative!"
5. Camel consumes the new S3 object and transforms it into a CloudEvent
6. Camel posts the CloudEvent to the Citrus local broker
7. Citrus receives the event and validates payload plus CloudEvent attributes
```

## Running the Tests

```bash
./mvnw -f quarkus/apache-camel/camel-knative/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

You can also run the module tests only:

```bash
./mvnw -f quarkus/apache-camel/camel-knative/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

## Key Dependencies

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-knative-producer</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-aws2-s3</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-kamelet</artifactId>
</dependency>
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-knative</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Knative Eventing](https://knative.dev/docs/eventing/) — Knative event-driven architecture concepts
- [Apache Camel Knative Component](https://camel.apache.org/components/latest/knative-component.html) — Camel Knative component reference
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/) — Pre-built Camel source/sink connectors
- [Apache Camel AWS S3 Source Kamelet](https://camel.apache.org/camel-kamelets/next/aws-s3-source.html)
- [Knative SinkBinding](https://knative.dev/docs/eventing/custom-event-source/sinkbinding/) — How `K_SINK` is injected in real Kubernetes deployments
- [CloudEvents Specification](https://cloudevents.io/) — CloudEvents standard
- [LocalStack](https://www.localstack.cloud/)

---

**Next Steps**: Extend the test to verify multiple CloudEvent types using trigger filters, or add a consumer side to see how Citrus can also simulate a Knative subscriber receiving events from the broker.

## Sample contract

**Purpose**: This example demonstrates how to test an Apache Camel Knative event source that turns Amazon S3 uploads into CloudEvents and sends them to a Knative broker, running locally with Citrus and LocalStack.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-knative`, `citrus-http`, `citrus-testcontainers`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Knative, HTTP, and Testcontainers actions.
**Prerequisites**: JDK 21 or newer. Docker must be running for Testcontainers or Quarkus Dev Services.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-knative/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-knative /tmp/citrus-camel-knative
cd /tmp/citrus-camel-knative
mvn clean verify -Dsystem.under.test.mode=embedded
```
