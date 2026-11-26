# Event-Driven Kafka Testing with Citrus

This example demonstrates how to test event-driven Quarkus applications using Citrus framework with Kafka message broker integration. The project showcases a simple message transformation workflow where incoming messages are converted to uppercase and published to an output topic.

## What You'll Learn

By the end of this guide, you'll understand:

- How to configure Citrus endpoints for Kafka topics
- How to write integration tests for event-driven applications
- How Citrus leverages Quarkus dev services for automatic Kafka setup
- The Gherkin-style test DSL (Given-When-Then) in Citrus
- How to send and receive messages on Kafka topics in tests

## The Application Under Test

The Quarkus application (`EventDrivenApplication.java`) implements a simple reactive message processing pipeline using SmallRye Reactive Messaging:

```
Kafka Topic (words-in) → Transform to Uppercase → Kafka Topic (words-out)
```

### Application Flow

1. **Message Reception**: The application listens on the `words-in` Kafka topic
2. **Transformation**: Each incoming message is transformed to uppercase using the `toUpperCase()` method
3. **Internal Processing**: The uppercase message flows through an internal `uppercase` channel
4. **Message Production**: The `sink()` method prefixes the message with `">> "` and publishes it to the `words-out` topic

**Key Code Snippet** (`EventDrivenApplication.java:22-26`):
```java
@Incoming("words-in")
@Outgoing("uppercase")
public String toUpperCase(String message) {
    return message.toUpperCase();
}
```

The application configuration (`application.properties`) maps the logical channel names to actual Kafka topics:
- `words-in` channel → `words-in` topic
- `words-out` channel → `words-out` topic

## Understanding the Citrus Test

The test class `EventDrivenApplicationTest` demonstrates how to verify the end-to-end message flow using Citrus framework.

### Test Setup: Endpoint Configuration

Citrus uses annotations to declaratively configure Kafka endpoints:

```java
@CitrusEndpoint
@KafkaEndpointConfig(topic = "words-in",
        server = "${kafka.bootstrap.servers}")
KafkaEndpoint wordsIn;

@CitrusEndpoint
@KafkaEndpointConfig(topic = "words-out",
        server = "${kafka.bootstrap.servers}")
KafkaEndpoint wordsOut;
```

**What's happening here:**

- `@CitrusEndpoint`: Marks fields as Citrus test endpoints that will be automatically configured
- `@KafkaEndpointConfig`: Specifies the Kafka topic name and broker connection details
- `${kafka.bootstrap.servers}`: A property automatically provided by Quarkus dev services pointing to the test Kafka broker
- Two endpoints are defined: one for sending messages (`wordsIn`) and one for receiving (`wordsOut`)

### Test Execution: Gherkin-Style DSL

The actual test uses Citrus's Gherkin-style test runner for readable Given-When-Then syntax:

```java
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
```

**Test Flow Breakdown:**

1. **WHEN**: Send a message with body `"Hi"` to the `words-in` topic
2. **THEN**: Verify that a message with body `">> HI"` is received from the `words-out` topic

The test validates:
- The message was successfully processed by the application
- The transformation logic (uppercase conversion) works correctly
- The output formatting (prefix `">> "`) is applied

## Key Testing Concepts

### 1. Integration with Quarkus Dev Services

Quarkus automatically starts a Kafka broker container when running tests (via Testcontainers). The connection details are exposed through the `kafka.bootstrap.servers` property, which Citrus uses to connect to the same broker instance. This ensures:

- No manual Kafka setup required
- Tests run against a real Kafka broker (not mocks)
- Automatic cleanup after test execution
- Isolation between test runs

### 2. Citrus Kafka Module

The `citrus-kafka` Maven dependency is added to the Maven POM:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-kafka</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module adds:
- Kafka endpoint implementations
- Producer and consumer capabilities
- Message serialization/deserialization

### 3. Test Annotations

The test class uses several key annotations to integrate Quarkus with Citrus:

```java
@QuarkusTest
@CitrusSupport
class EventDrivenApplicationTest implements TestActionSupport {

    @CitrusResource
    GherkinTestActionRunner runner;
    
    // ... endpoint configurations and tests
}
```

**Annotation breakdown:**

- `@QuarkusTest`: Starts the Quarkus application in test mode and activates dev services
- `@CitrusSupport`: Enables Citrus framework integration with Quarkus, allowing Citrus endpoints and test runners to work seamlessly
- `@CitrusResource`: Injects the Citrus test runner (`GherkinTestActionRunner`) for executing test actions with Given-When-Then syntax
- `TestActionSupport`: Interface that provides convenient static imports for test actions like `send()`, `receive()`, `echo()`, etc.

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/event-driven-kafka/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Quarkus dev services automatically start a Kafka container
3. Citrus configures the Kafka endpoints using the container's connection details
4. The test sends a message to `words-in`
5. The application processes the message and publishes to `words-out`
6. Citrus receives and validates the output message
7. The test completes, and Quarkus stops the Kafka container

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **quarkus-messaging-kafka**: Provides Kafka reactive messaging support
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-kafka**: Adds Kafka endpoint support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Quarkus Kafka Guide](https://quarkus.io/guides/kafka-getting-started) - Getting started with Kafka in Quarkus
- [Citrus Kafka Module](https://citrusframework.org/docs/endpoints/kafka/) - Kafka endpoint reference
- [Quarkus Dev Services](https://quarkus.io/guides/dev-services) - Automatic service provisioning for tests

---

**Next Steps**: Try modifying the test to validate different message transformations, error scenarios, or message headers. Explore the Citrus documentation to learn about advanced features like message validators, test variables, and complex test flows.

## Sample contract

**Purpose**: This example demonstrates how to test event-driven Quarkus applications using Citrus framework with Kafka message broker integration.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-kafka`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Kafka actions.
**Prerequisites**: JDK 21 or newer. Docker must be running for Testcontainers or Quarkus Dev Services.
**Run from repository root**:

```shell
./mvnw -f quarkus/event-driven-kafka/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/event-driven-kafka /tmp/citrus-event-driven-kafka
cd /tmp/citrus-event-driven-kafka
mvn clean verify -Dsystem.under.test.mode=embedded
```
