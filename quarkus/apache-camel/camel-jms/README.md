# Apache Camel JMS Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes in Quarkus applications using Citrus framework with JMS (ActiveMQ Artemis) integration. The project showcases a simple Camel route that transforms incoming JMS messages to uppercase and publishes them to an output queue.

## What You'll Learn

By the end of this guide, you'll understand:

- How to create Apache Camel routes in Quarkus for JMS message processing
- How to configure Citrus endpoints for testing Camel JMS routes
- How to write integration tests for Camel-based applications
- How to share the JMS connection factory between Quarkus, Camel, and Citrus using `@BindToRegistry`
- How to use Quarkus test resources to automatically provision an Artemis broker
- The Gherkin-style test DSL (Given-When-Then) in Citrus for testing Camel routes
- How Apache Camel simplifies integration patterns in Quarkus

## The Application Under Test

The Quarkus application uses Apache Camel to implement a simple integration route with ActiveMQ Artemis JMS:

```
JMS Queue (words-in) → Apache Camel Route (Transform) → JMS Queue (words-out)
```

### Apache Camel Route

The application consists of a single, elegant Camel route defined in `Routes.java`:

```java
public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jms:words-in")
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to("jms:words-out");
    }
}
```

**Route Breakdown:**

1. **from("jms:words-in")**: Camel consumer that listens to the `words-in` JMS queue
2. **.setBody(...)**: Transformation step that:
   - Retrieves the message body from the exchange
   - Converts it to uppercase
   - Prefixes it with `">> "`
3. **to("jms:words-out")**: Camel producer that sends the transformed message to the `words-out` JMS queue

### Why Apache Camel?

Apache Camel provides:

- **Concise Route Definition**: Complex integration flows in minimal code
- **Rich Component Library**: 300+ connectors for various protocols and systems
- **Enterprise Integration Patterns (EIP)**: Built-in implementations of proven patterns
- **Routing & Mediation**: Message routing, transformation, and orchestration
- **Testing Support**: Integration-friendly testing with Citrus

In this example, the entire integration flow (consume → transform → produce) is expressed in just 3 lines of fluent API code, compared to the multi-class approach required with raw reactive messaging.

### Camel JMS Component Configuration

Quarkus automatically configures the Camel JMS component to use the Artemis connection factory. The `jms:` prefix in the route definition (`from("jms:words-in")`) references this pre-configured component, which:

- Uses the Artemis connection factory injected by Quarkus
- Supports automatic reconnection and pooling
- Integrates seamlessly with Camel's error handling
- Requires no additional configuration code

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to verify the end-to-end Camel route flow using Citrus framework.

### Test Setup: Connection Factory Sharing

A key feature of this test is sharing the JMS connection factory between Quarkus, Apache Camel, and Citrus:

```java
@Inject
@BindToRegistry
ConnectionFactory connectionFactory;
```

**What's happening here:**

- `@Inject`: Quarkus injects the configured Artemis connection factory
- `@BindToRegistry`: Citrus annotation that registers this connection factory in the Citrus context, making it available to all Citrus JMS endpoints
- The same connection factory is used by:
  - Quarkus for dependency injection
  - Apache Camel for the `jms:` component
  - Citrus for test endpoints
- This ensures all three frameworks communicate with the same JMS broker instance

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
- Two endpoints are defined: one for sending messages to the Camel route (`wordsIn`) and one for receiving results (`wordsOut`)

### Test Setup: Artemis Test Resource

The test uses Quarkus test resources to provision an Artemis broker:

```java
@QuarkusTest
@CitrusSupport
@WithTestResource(ArtemisTestResource.class)
class QuarkusApplicationTest implements TestActionSupport {
    // ...
}
```

**What's happening here:**

- `@WithTestResource(ArtemisTestResource.class)`: Automatically starts an ActiveMQ Artemis container for testing via Testcontainers
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
2. The Camel route automatically consumes the message from `words-in`
3. The route transforms the message to `">> HOWDY"`
4. The route produces the message to the `words-out` queue
5. **THEN**: Verify that a message with body `">> HOWDY"` is received from the `words-out` queue

The test validates:
- The Camel route successfully consumes messages from the JMS queue
- The transformation logic (uppercase conversion + prefix) works correctly
- The Camel route successfully produces messages to the output queue
- End-to-end integration between JMS, Camel, and the application

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

### 3. Apache Camel Quarkus Dependencies

The application uses Camel Quarkus extensions:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-jms</artifactId>
</dependency>
```

This provides:
- Apache Camel JMS component for Quarkus
- Automatic configuration of Camel with Quarkus CDI
- Native image support for Camel routes
- Integration with Quarkus lifecycle management

The `quarkus-camel-bom` in dependency management ensures compatible versions of all Camel Quarkus extensions.

### 4. Test Annotations

The test class uses several key annotations to integrate Quarkus, Camel, and Citrus:

```java
@QuarkusTest
@CitrusSupport
@WithTestResource(ArtemisTestResource.class)
class QuarkusApplicationTest implements TestActionSupport {

    @Inject
    @BindToRegistry
    ConnectionFactory connectionFactory;

    @CitrusResource
    GherkinTestActionRunner runner;

    // ... endpoint configurations and tests
}
```

**Annotation breakdown:**

- `@QuarkusTest`: Starts the Quarkus application (including Camel routes) in test mode
- `@CitrusSupport`: Enables Citrus framework integration with Quarkus, allowing Citrus endpoints and test runners to work seamlessly
- `@WithTestResource(ArtemisTestResource.class)`: Provisions an ActiveMQ Artemis broker for testing using Testcontainers
- `@BindToRegistry`: Registers the injected connection factory in Citrus's context, making it available to all JMS endpoints (and automatically picked up by Camel)
- `@CitrusResource`: Injects the Citrus test runner (`GherkinTestActionRunner`) for executing test actions with Given-When-Then syntax
- `TestActionSupport`: Interface that provides convenient static imports for test actions like `send()`, `receive()`, `echo()`, etc.

### 5. Apache Camel Route Discovery

Quarkus automatically discovers and registers Camel routes:

- Any class extending `RouteBuilder` in the classpath is detected
- Routes are started when the Quarkus application starts
- In test mode, routes are fully functional and process real messages
- No additional configuration or registration code needed

### 6. Connection Factory Configuration

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

### 7. Synchronous Testing of Asynchronous Camel Routes

While Camel routes process messages asynchronously, the Citrus test runs synchronously:

1. The `send()` action publishes a message to the JMS queue
2. Camel asynchronously consumes, transforms, and produces the message
3. The `receive()` action blocks until a message arrives on the output queue (or timeout occurs)
4. This approach ensures deterministic test execution and proper verification

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-jms/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Apache Camel routes are discovered and started
3. The Artemis test resource starts an ActiveMQ Artemis container via Testcontainers
4. Quarkus configures the connection factory to connect to the test broker
5. Camel's JMS component automatically uses this connection factory
6. Citrus registers the connection factory for use by JMS endpoints
7. The test sends a message to `words-in` queue
8. The Camel route consumes, transforms, and produces the message to `words-out` queue
9. Citrus receives and validates the output message
10. The test completes, and the Artemis container is stopped

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **quarkus-artemis-jms**: Integrates ActiveMQ Artemis JMS with Quarkus
- **camel-quarkus-jms**: Apache Camel JMS component for Quarkus
- **quarkus-test-artemis**: Provides Artemis test resource for automatic broker provisioning
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-jms**: Adds JMS endpoint support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Apache Camel Quarkus JMS Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/jms.html) - Camel JMS component reference
- [Apache Camel Quarkus Core Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html) - Camel core functionality
- [Quarkus Artemis JMS Guide](https://docs.quarkiverse.io/quarkus-artemis/dev/quarkus-artemis-jms.html) - Getting started with Artemis JMS in Quarkus
- [Citrus JMS Module](https://citrusframework.org/docs/endpoints/jms/) - JMS endpoint reference
- [Apache Camel Documentation](https://camel.apache.org/) - Official Camel documentation

---

**Next Steps**: Try adding more complex Camel patterns like content-based routing, message filters, or aggregation. Explore combining Camel with other Citrus endpoints (HTTP, file system) to test multi-protocol integration scenarios. Learn about Camel's error handling and retry mechanisms and how to test them with Citrus.

## Sample contract

**Purpose**: This example demonstrates how to test Apache Camel routes in Quarkus applications using Citrus framework with JMS (ActiveMQ Artemis) integration.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-jms`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with JMS actions.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-jms/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-jms /tmp/citrus-camel-jms
cd /tmp/citrus-camel-jms
mvn clean verify -Dsystem.under.test.mode=embedded
```
