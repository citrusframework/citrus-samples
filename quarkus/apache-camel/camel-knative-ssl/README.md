# Apache Camel Knative Eventing with SSL/TLS and Citrus

This example demonstrates how to test an Apache Camel Knative event source that turns Amazon S3 uploads into CloudEvents and sends them to a Knative broker **secured with TLS/SSL**, running locally with Citrus and LocalStack.

It follows the same flow as the [`camel-knative`](../camel-knative) example but the broker endpoint is HTTPS and Citrus validates the received CloudEvents over a secure channel.

## What You'll Learn

By the end of this guide, you'll understand:

- **SSL-secured Knative broker testing**: How Citrus starts a Jetty-backed HTTPS server acting as a local Knative broker so you can verify TLS-protected event delivery without a Kubernetes cluster
- **Camel SSL client configuration**: How to wire a `KnativeSslClientOptions` CDI bean into the Knative component so Camel connects to the HTTPS broker
- **S3 event simulation**: How LocalStack provides an S3-compatible service for exercising the Camel `aws-s3-source` Kamelet in tests
- **CloudEvent verification over HTTPS**: How to assert Knative CloudEvent attributes using Citrus `http().server().receive()` actions on a secure port

## The Application Under Test

The Quarkus application uses Apache Camel to consume objects from an S3 bucket and publish them as CloudEvents to a TLS-secured Knative broker:

```
Amazon S3 upload → Camel aws-s3-source Kamelet → CloudEvent transform → HTTPS Knative broker
```

### Apache Camel Route

The route is defined in [`Routes.java`](src/main/java/org/citrusframework/samples/quarkus/camel/knative/ssl/Routes.java):

```java
from("kamelet:aws-s3-source")
        .transformDataType(new DataType("http:application-cloudevents"))
        .to("knative:event/org.apache.camel.event.messages?kind=Broker&name=default");
```

The route itself is unchanged from the plain `camel-knative` example — the SSL wiring is handled entirely through the CDI bean and configuration below.

### SSL Client Bean

[`SourceOptions.java`](src/main/java/org/citrusframework/samples/quarkus/camel/knative/ssl/SourceOptions.java) provides a CDI bean that supplies the SSL-aware HTTP client options to the Knative component:

```java
@ApplicationScoped
public class SourceOptions {

    @Named("knativeHttpClientOptions")
    public WebClientOptions knativeHttpClientOptions(CamelContext camelContext) {
        return new KnativeSslClientOptions(camelContext);
    }
}
```

### Camel Knative Configuration

[`application.properties`](src/main/resources/application.properties) wires the SSL client bean and points the broker sink to HTTPS:

```properties
camel.component.knative.environment-path=classpath:knative.json
camel.component.knative.producerFactory.clientOptions=#bean:knativeHttpClientOptions

camel.component.knative.ceOverride[ce-type]=dev.knative.eventing.aws-s3
camel.component.knative.ceOverride[ce-source]=dev.knative.eventing.aws-s3-source
camel.component.knative.ceOverride[ce-subject]=aws-s3-source

%test.k.sink=https://localhost:8443
```

The [`knative.json`](src/main/resources/knative.json) file defines the broker sink using `https`:

```json
{
  "resources": [
    {
      "name": "default",
      "type": "event",
      "endpointKind": "sink",
      "url": "{{k.sink:https://localhost:8443}}",
      "objectApiVersion": "eventing.knative.dev/v1",
      "objectKind": "Broker",
      "objectName": "default"
    }
  ]
}
```

## Understanding the Citrus Test

The test class [`QuarkusApplicationTest`](src/test/java/org/citrusframework/samples/quarkus/camel/knative/ssl/QuarkusApplicationTest.java) verifies the end-to-end flow by combining a local HTTPS Knative broker with a LocalStack-backed S3 bucket.

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
- `ContainerLifecycleListener<LocalStackContainer>`: injects dynamic S3 connection properties and SSL configuration properties into the Camel configuration when the container starts

### SSL Broker Registration

```java
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
```

`@BindToRegistry` registers the server in the Citrus context. The server listens on the plain port `8080` and the secure port `8443`, using the test keystores from `src/test/resources/keystore/`.

### Injecting SSL Properties at Runtime

When the LocalStack container starts, the [`started()`](src/test/java/org/citrusframework/samples/quarkus/camel/knative/ssl/QuarkusApplicationTest.java) callback injects both the S3 connection properties and the SSL client properties into the running Quarkus application:

```java
return Map.ofEntries(
    // S3 connection
    Map.entry("camel.kamelet.aws-s3-source.accessKey", container.getAccessKey()),
    ...
    // SSL handshake
    Map.entry("camel.knative.client.ssl.enabled", "true"),
    Map.entry("camel.knative.client.ssl.verify.hostname", "false"),
    Map.entry("camel.knative.client.ssl.key.path", "keystore/client.pem"),
    Map.entry("camel.knative.client.ssl.key.cert.path", "keystore/client.crt"),
    Map.entry("camel.knative.client.ssl.truststore.path", "keystore/truststore.jks"),
    Map.entry("camel.knative.client.ssl.truststore.password", "secr3t")
);
```

### CloudEvent Validation

The test uses explicit `http().server()` receive/send actions to validate the POST request and respond with `200 OK`:

```java
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
```

### Test Keystore Files

The module ships pre-generated test keystores under `src/test/resources/keystore/`:

| File | Contents |
|------|----------|
| `server.jks` | Server keystore (password: `secr3t`) — used by Citrus Jetty server |
| `truststore.jks` | Truststore containing the test CA (password: `secr3t`) — used by both server and Camel client |
| `client.pem` | Client private key — used by `KnativeSslClientOptions` |
| `client.crt` | Client certificate — used by `KnativeSslClientOptions` |

All certificates are self-signed and for test use only.

### Test Flow

```
1. Citrus starts an HTTPS broker (knativeBroker) on localhost:8443 with server.jks
2. LocalStack starts an S3 service and the test creates bucket knative-bucket
3. Camel aws-s3-source connects to LocalStack using the injected test properties
4. SSL client properties are injected so KnativeSslClientOptions trusts the test CA
5. The test uploads message.txt with content "Hello Knative!"
6. Camel consumes the new S3 object and transforms it into a CloudEvent
7. Camel posts the CloudEvent to https://localhost:8443 over TLS
8. Citrus receives the CloudEvent, validates the payload and CloudEvent headers
9. Citrus sends 200 OK to complete the HTTP exchange
```

## Running the Tests

```bash
./mvnw -f quarkus/apache-camel/camel-knative-ssl/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

You can also run the module tests only:

```bash
./mvnw -f quarkus/apache-camel/camel-knative-ssl/pom.xml clean verify -Dsystem.under.test.mode=embedded
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
    <artifactId>citrus-http</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

## Related Resources

- [camel-knative example](../camel-knative) — the plain (non-SSL) version of this example
- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Knative Eventing](https://knative.dev/docs/eventing/) — Knative event-driven architecture concepts
- [Apache Camel Knative Component](https://camel.apache.org/components/latest/knative-component.html) — Camel Knative component reference
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/) — Pre-built Camel source/sink connectors
- [Apache Camel AWS S3 Source Kamelet](https://camel.apache.org/camel-kamelets/next/aws-s3-source.html)
- [Knative SinkBinding](https://knative.dev/docs/eventing/custom-event-source/sinkbinding/) — How `K_SINK` is injected in real Kubernetes deployments
- [CloudEvents Specification](https://cloudevents.io/) — CloudEvents standard
- [LocalStack](https://www.localstack.cloud/)

## Sample contract

**Purpose**: This example demonstrates how to test an Apache Camel Knative event source that turns Amazon S3 uploads into CloudEvents and sends them to a Knative broker **secured with TLS/SSL**, running locally with Citrus and LocalStack.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-http`, `citrus-testcontainers`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with HTTP and Testcontainers actions.
**Prerequisites**: JDK 21 or newer. Docker must be running for Testcontainers or Quarkus Dev Services.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-knative-ssl/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-knative-ssl /tmp/citrus-camel-knative-ssl
cd /tmp/citrus-camel-knative-ssl
mvn clean verify -Dsystem.under.test.mode=embedded
```
