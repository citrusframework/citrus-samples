# Apache Camel MQTT Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes in Quarkus applications using the Citrus framework with MQTT and Kafka integration. The project showcases a Camel route that consumes messages from an MQTT broker via the `mqtt5-source` Kamelet, applies the **Content Based Router** EIP to evaluate the message body using JQ, and routes messages to different Kafka topics based on the temperature value.

## Why Citrus for MQTT Testing?

Citrus does not provide a native MQTT endpoint implementation. Instead, this example demonstrates how Citrus can leverage **Apache Camel's MQTT component** (`paho-mqtt5`) to send MQTT messages during integration tests. This approach:

- Reuses Camel's mature MQTT client implementation
- Eliminates the need for a dedicated MQTT testing library
- Integrates seamlessly with the Citrus test DSL via `CamelSupport.camel()`

For Kafka verification, Citrus provides the `@KafkaContainerSupport` annotation and `@KafkaEndpointConfig` - a simple, declarative way to provision Kafka broker infrastructure and configure test endpoints:

```java
@KafkaContainerSupport(port = 9092, version = "4.2.0")
class QuarkusApplicationTest {
    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "temperature-warm")
    KafkaEndpoint temperatureWarm;
}
```

## What You'll Learn

By the end of this guide, you'll understand:

- **Camel MQTT via Kamelet**: How to consume MQTT messages using the `mqtt5-source` Kamelet
- **Content Based Router EIP**: How to route messages to different Kafka topics based on body content
- **JQ Transformation**: How to extract values from JSON message bodies using JQ expressions
- **Citrus + Camel MQTT Testing**: How to use Camel's `paho-mqtt5` component through Citrus for sending MQTT messages in tests
- **Testcontainers for MQTT**: How to use `@TestcontainersSupport` with a generic Mosquitto container
- **Multi-Protocol Testing**: How to combine MQTT (send) and Kafka (receive/verify) in a single integration test

## The Application Under Test

The Quarkus application uses Apache Camel to implement an integration route that bridges MQTT and Kafka:

```
MQTT Topic (temperature) → JQ Transform (.value) → Content Based Router → Kafka Topic (temperature-warm / temperature-cold)
```

### Apache Camel Route

The application consists of a single Camel route defined in `Routes.java`:

```java
public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:mqtt5-source?brokerUrl={{mqtt.broker.url}}&topic={{mqtt.topic}}")
            .transform().jq(".value")
            .convertBodyTo(Integer.class)
            .choice()
                .when().simple("${body} > 20")
                    .log("Warm temperature: ${body}")
                    .to(kafka("temperature-warm"))
                .otherwise()
                    .log("Cold temperature: ${body}")
                    .to(kafka("temperature-cold"))
            .end();
    }
}
```

**Route Breakdown:**

1. **from("kamelet:mqtt5-source?...")**: Consumes messages from the MQTT broker using the `mqtt5-source` Kamelet
   - `brokerUrl` and `topic` are resolved from application properties via Camel property placeholders (`{{...}}`)
2. **.transform().jq(".value")**: Extracts the `value` field from the JSON body using a JQ expression
3. **.convertBodyTo(Integer.class)**: Converts the extracted value to an integer for numeric comparison
4. **.choice()**: Content Based Router EIP that evaluates the temperature value
   - **when body > 20**: Routes to the `temperature-warm` Kafka topic
   - **otherwise**: Routes to the `temperature-cold` Kafka topic

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to verify the end-to-end routing behavior using Citrus framework.

### Test Setup: MQTT Broker via Testcontainers

Since Citrus doesn't have built-in MQTT container support, the test uses `@TestcontainersSupport` with a custom `GenericContainerProvider` to start a Mosquitto MQTT broker:

```java
@TestcontainersSupport(
    containerProvider = QuarkusApplicationTest.MosquittoContainerProvider.class,
    containerLifecycleListener = QuarkusApplicationTest.MosquittoConfigurer.class)
class QuarkusApplicationTest {

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
            String brokerUrl = "tcp://%s:%d".formatted(
                container.getHost(), container.getMappedPort(MOSQUITTO_PORT));
            return Map.of("mqtt.broker.url", brokerUrl);
        }
    }
}
```

**What's happening here:**

- `GenericContainerProvider`: Creates a Mosquitto container with anonymous access enabled
- `MosquittoConfigurer`: Returns the dynamic broker URL as a Quarkus application property (`mqtt.broker.url`), which Camel resolves via its property placeholder mechanism

### Test Setup: Kafka Broker and Topics

The test uses `@KafkaContainerSupport` for the Kafka broker and a lifecycle listener to create the required topics:

```java
@KafkaContainerSupport(port = 9092, version = "4.2.0",
    containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
```

### Sending MQTT Messages via Camel

Since Citrus has no native MQTT endpoint, the test uses Camel's `paho-mqtt5` component through `CamelSupport.camel()`:

```java
import static org.citrusframework.camel.dsl.CamelSupport.camel;

runner.when(
    camel()
        .send()
        .endpoint("paho-mqtt5:temperature?brokerUrl={{mqtt.broker.url}}")
        .message()
        .fork(true)
        .body("{\"value\": 25}")
);
```

**What's happening here:**

- `camel().send()`: Uses Citrus's Camel integration to send a message through a Camel endpoint
- `.endpoint("paho-mqtt5:temperature?brokerUrl={{mqtt.broker.url}}")`: Specifies the Camel MQTT endpoint URI with Camel property placeholders
- `.fork(true)`: Sends the message asynchronously so the test can proceed to verify the Kafka output
- The Camel context resolves `{{mqtt.broker.url}}` from Quarkus application properties (set by `MosquittoConfigurer`)

### Verifying Kafka Output

The test verifies that messages arrive on the correct Kafka topic using standard Citrus Kafka endpoints:

```java
@CitrusEndpoint
@KafkaEndpointConfig(topic = "temperature-warm", consumerGroup = "citrus-consumer-1")
KafkaEndpoint temperatureWarm;

runner.then(
    receive()
        .endpoint(temperatureWarm)
        .message()
        .body("25")
);
```

### Test Execution Flow

1. **WHEN**: Send `{"value": 25}` via MQTT to the `temperature` topic
2. The Camel route consumes the MQTT message via the `mqtt5-source` Kamelet
3. JQ extracts the value `25` from the JSON body
4. The Content Based Router evaluates `25 > 20` → routes to `temperature-warm`
5. **THEN**: Verify that the message `25` arrives on the `temperature-warm` Kafka topic

## Key Testing Concepts

### 1. Citrus Camel Integration for Protocol Bridging

When Citrus doesn't provide a native endpoint for a protocol (like MQTT), you can leverage Camel's extensive component library through `CamelSupport.camel()`. This gives you access to 300+ Camel components from within Citrus tests.

### 2. Generic Container Support

The `@TestcontainersSupport` annotation with `GenericContainerProvider` allows you to use any Docker image as test infrastructure - not just the pre-built container types (Kafka, PostgreSQL, etc.).

### 3. Property-Based Configuration

Both the Camel route and the test MQTT endpoint use Camel property placeholders (`{{mqtt.broker.url}}`). The `ContainerLifecycleListener` returns properties that are automatically injected into the Quarkus application context, ensuring the Camel context resolves them correctly at runtime.

### 4. Forked Message Sending

The `.fork(true)` option is important when the send operation might block (e.g., waiting for MQTT acknowledgment). It ensures the test thread continues to the verification step while the message is being delivered asynchronously.

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-mqtt/pom.xml clean test
```

**What happens during test execution:**

1. `@TestcontainersSupport` starts a Mosquitto MQTT broker container
2. `@KafkaContainerSupport` starts a Kafka broker container
3. The `MosquittoConfigurer` injects the dynamic MQTT broker URL into Quarkus properties
4. The `KafkaConfigurer` creates `temperature-warm` and `temperature-cold` topics
5. Quarkus starts the application with the Camel route connecting to both brokers
6. The test sends JSON messages via MQTT and verifies they arrive on the correct Kafka topics

**Expected output:**
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Apache Camel Paho MQTT 5 Component](https://camel.apache.org/components/latest/paho-mqtt5-component.html) - Camel MQTT component reference
- [Apache Camel Content Based Router](https://camel.apache.org/components/latest/eips/choice-eip.html) - Content Based Router EIP
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/latest/) - Kamelet catalog
- [Eclipse Mosquitto](https://mosquitto.org/) - MQTT broker used in the test
