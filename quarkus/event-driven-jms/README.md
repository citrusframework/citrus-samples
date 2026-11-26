# Event-Driven JMS Testing with Citrus

This example demonstrates how to test event-driven Quarkus applications using Citrus framework with JMS (ActiveMQ Artemis) integration. The project showcases a simple message transformation workflow where incoming JMS messages are converted to uppercase and published to an output queue.

## What You'll Learn

By the end of this guide, you'll understand:

- How to configure Citrus endpoints for JMS queues
- How to write integration tests for JMS-based event-driven applications
- How to share the JMS connection factory between Quarkus and Citrus using `@BindToRegistry`
- How to use Quarkus test resources to automatically provision an Artemis broker
- The Gherkin-style test DSL (Given-When-Then) in Citrus for JMS testing
- How to send and receive messages on JMS queues in tests

## The Application Under Test

The Quarkus application implements a reactive message processing pipeline using SmallRye Reactive Messaging combined with ActiveMQ Artemis JMS:

```
JMS Queue (words-in) → Transform to Uppercase → JMS Queue (words-out)
```

### Application Flow

The application consists of three main components:

1. **EventDrivenApplication** (`EventDrivenApplication.java`): Transforms messages to uppercase and routes them internally
2. **WordConsumer** (`WordConsumer.java`): Consumes messages from the `words-in` JMS queue and emits them to the internal `words-in` channel
3. **WordProducer** (`WordProducer.java`): Sends the processed messages to the `words-out` JMS queue

#### Component Details

**EventDrivenApplication** - Message transformation:
```java
@Incoming("words-in")
@Outgoing("uppercase")
public String toUpperCase(String message) {
    return message.toUpperCase();
}

@Incoming("uppercase")
public void sink(String word) {
    producer.send(">> " + word);
}
```

**WordConsumer** - Bridges JMS to Reactive Messaging:
```java
@Override
public void run() {
    try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
        JMSConsumer consumer = context.createConsumer(context.createQueue("words-in"));
        while (true) {
            Message message = consumer.receive();
            if (message == null) return;
            emitter.send(message.getBody(String.class));
        }
    }
}
```

**WordProducer** - Sends to JMS queue:
```java
public void send(String word) {
    try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
        context.createProducer().send(context.createQueue("words-out"), word);
    }
}
```

## Understanding the Citrus Test

The test class `EventDrivenApplicationTest` demonstrates how to verify the end-to-end JMS message flow using Citrus framework.

### Test Setup: Connection Factory Sharing

A key feature of this test is sharing the JMS connection factory between Quarkus and Citrus:

```java
@Inject
@BindToRegistry
ConnectionFactory connectionFactory;
```

**What's happening here:**

- `@Inject`: Quarkus injects the configured Artemis connection factory
- `@BindToRegistry`: Citrus annotation that registers this connection factory in the Citrus context, making it available to all Citrus JMS endpoints
- This ensures both the application and the test use the same JMS broker connection

### Test Setup: Endpoint Configuration

Citrus uses annotations to declaratively configure JMS endpoints:

```java
@CitrusEndpoint
@JmsEndpointConfig(destinationName = "words-in")
JmsEndpoint wordsIn;

@CitrusEndpoint
@JmsEndpointConfig(destinationName = "words-out")
JmsEndpoint wordsOut;
```

**What's happening here:**

- `@CitrusEndpoint`: Marks fields as Citrus test endpoints that will be automatically configured
- `@JmsEndpointConfig`: Specifies the JMS destination (queue) name
- The connection factory is automatically resolved from the Citrus registry (registered via `@BindToRegistry`)
- Two endpoints are defined: one for sending messages (`wordsIn`) and one for receiving (`wordsOut`)

### Test Setup: Artemis Test Resource

The test uses Quarkus test resources to provision an Artemis broker:

```java
@QuarkusTest
@CitrusSupport
@WithTestResource(ArtemisTestResource.class)
class EventDrivenApplicationTest implements TestActionSupport {
    // ...
}
```

**What's happening here:**

- `@WithTestResource(ArtemisTestResource.class)`: Automatically starts an ActiveMQ Artemis container for testing
- The test resource configures the connection factory to point to this test broker
- No manual broker setup required
- Automatic cleanup after test execution

### Test Execution: Gherkin-Style DSL

The actual test uses Citrus's Gherkin-style test runner for readable Given-When-Then syntax:

```java
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
```

**Test Flow Breakdown:**

1. **WHEN**: Send a message with body `"Howdy"` to the `words-in` queue
2. **THEN**: Verify that a message with body `">> HOWDY"` is received from the `words-out` queue

The test validates:
- The message was successfully consumed from the JMS queue
- The transformation logic (uppercase conversion) works correctly
- The output formatting (prefix `">> "`) is applied
- The message was successfully published to the output queue

## Key Testing Concepts

### 1. Integration with Quarkus Artemis Test Resource

The `quarkus-test-artemis` module provides automatic broker provisioning:

```xml
<dependency>
    <groupId>io.quarkiverse.artemis</groupId>
    <artifactId>quarkus-test-artemis</artifactId>
    <scope>test</scope>
</dependency>
```

This ensures:
- No manual Artemis broker setup required
- Tests run against a real JMS broker (via Testcontainers)
- Automatic cleanup after test execution
- Isolation between test runs

### 2. Citrus JMS Module

The `citrus-jms` Maven dependency is added to the Maven POM:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-jms</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module adds:
- JMS endpoint implementations
- Message producer and consumer capabilities
- JMS message header and property validation
- Integration with Citrus test framework

### 3. Test Annotations

The test class uses several key annotations to integrate Quarkus with Citrus:

```java
@QuarkusTest
@CitrusSupport
@WithTestResource(ArtemisTestResource.class)
class EventDrivenApplicationTest implements TestActionSupport {

    @Inject
    @BindToRegistry
    ConnectionFactory connectionFactory;

    @CitrusResource
    GherkinTestActionRunner runner;
    
    // ... endpoint configurations and tests
}
```

**Annotation breakdown:**

- `@QuarkusTest`: Starts the Quarkus application in test mode
- `@CitrusSupport`: Enables Citrus framework integration with Quarkus, allowing Citrus endpoints and test runners to work seamlessly
- `@WithTestResource(ArtemisTestResource.class)`: Provisions an ActiveMQ Artemis broker for testing using Testcontainers
- `@BindToRegistry`: Registers the injected connection factory in Citrus's context, making it available to all JMS endpoints
- `@CitrusResource`: Injects the Citrus test runner (`GherkinTestActionRunner`) for executing test actions with Given-When-Then syntax
- `TestActionSupport`: Interface that provides convenient static imports for test actions like `send()`, `receive()`, `echo()`, etc.

### 4. Connection Factory Configuration

The application configuration differs between production and test:

**Production** (`application.properties`):
```properties
quarkus.artemis.url=tcp://localhost:61616
quarkus.artemis.username=quarkus
quarkus.artemis.password=quarkus
```

**Test** (`test/resources/application.properties`):
```properties
%test.quarkus.artemis.username=
%test.quarkus.artemis.password=
```

The test configuration removes authentication because the Artemis test resource starts an unauthenticated broker by default.

### 5. Synchronous Testing of Asynchronous Systems

While the application processes messages asynchronously across multiple components, the Citrus test runs synchronously:

1. The `send()` action publishes a message to the JMS queue
2. The application asynchronously consumes, transforms, and produces the message
3. The `receive()` action blocks until a message arrives on the output queue (or timeout occurs)
4. This approach ensures deterministic test execution and proper verification

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/event-driven-jms/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. The Artemis test resource starts an ActiveMQ Artemis container via Testcontainers
3. Quarkus configures the connection factory to connect to the test broker
4. Citrus registers the connection factory for use by JMS endpoints
5. The test sends a message to `words-in` queue
6. The application consumes, transforms, and produces the message to `words-out` queue
7. Citrus receives and validates the output message
8. The test completes, and the Artemis container is stopped

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **quarkus-messaging**: Provides SmallRye Reactive Messaging support
- **quarkus-artemis-jms**: Integrates ActiveMQ Artemis JMS with Quarkus
- **quarkus-test-artemis**: Provides Artemis test resource for automatic broker provisioning
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-jms**: Adds JMS endpoint support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Quarkus Artemis JMS Guide](https://docs.quarkiverse.io/quarkus-artemis/dev/quarkus-artemis-jms.html) - Getting started with Artemis JMS in Quarkus
- [Citrus JMS Module](https://citrusframework.org/docs/endpoints/jms/) - JMS endpoint reference
- [ActiveMQ Artemis](https://activemq.apache.org/components/artemis/) - Official Artemis documentation

---

**Next Steps**: Try modifying the test to validate JMS message headers, selectors, or different message types (TextMessage, ObjectMessage). Explore the Citrus documentation to learn about advanced features like message validators, test variables, and complex test flows.
