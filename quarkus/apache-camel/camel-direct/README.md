# Apache Camel Direct Endpoint Testing with Citrus

This example demonstrates how to test Apache Camel routes using Citrus framework with **shared CamelContext integration**. The project showcases direct Camel endpoint testing using Camel's `direct:` component for synchronous in-memory message passing and `mock:` endpoints for verification.

## What You'll Learn

By the end of this guide, you'll understand:

- How to create Apache Camel routes using `direct:` and `mock:` endpoints
- How to share the CamelContext between Quarkus, Camel, and Citrus using `@BindToRegistry`
- How to test Camel routes directly without external message brokers
- How to use Camel's mock endpoints for verification in integration tests
- How to send messages to Camel routes using Citrus's `camel:` endpoint URI
- How to combine Citrus test actions with Camel's MockEndpoint assertions
- The benefits of in-memory Camel testing for fast, isolated unit/integration tests

## The Application Under Test

The Quarkus application uses Apache Camel to implement a simple in-memory integration route:

```
Direct Endpoint (direct:words-in) → Transform → Mock Endpoint (mock:words-out)
```

### Apache Camel Route

The application consists of a single Camel route defined in `Routes.java`:

```java
public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:words-in")
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to("mock:words-out");
    }
}
```

**Route Breakdown:**

1. **from("direct:words-in")**: Direct endpoint for synchronous in-memory message passing
   - No external transport required (JMS, Kafka, HTTP, etc.)
   - Messages are passed directly between Camel routes or from external callers
   - Synchronous: the caller blocks until the route completes
   
2. **.setBody(...)**: Transformation step that:
   - Retrieves the message body from the exchange
   - Converts it to uppercase
   - Prefixes it with `">> "`
   
3. **to("mock:words-out")**: Mock endpoint for testing and verification
   - Captures all messages sent to it
   - Allows assertions on message count, content, headers, etc.
   - Commonly used in Camel tests but can also be used in production for early-stage development

### Understanding Camel Direct Component

The `direct:` component provides:

- **In-Memory Communication**: No network overhead, no external dependencies
- **Synchronous Invocation**: Caller waits for route completion
- **Same CamelContext**: Only works within a single CamelContext instance
- **Thread Model**: Route executes in the caller's thread
- **Use Cases**: 
  - Connecting multiple routes together
  - Breaking complex routes into smaller, testable pieces
  - Testing routes without external systems
  - Building modular integration architectures

### Understanding Camel Mock Component

The `mock:` component provides:

- **Message Capture**: Records all messages sent to it
- **Flexible Assertions**: Verify message count, bodies, headers, order, etc.
- **Test-Friendly**: Designed specifically for testing scenarios
- **Reset Capability**: Can be reset between tests
- **Use Cases**:
  - Verifying route outputs in tests
  - Early-stage development before real endpoints are implemented
  - Validating complex routing logic

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to test Camel routes directly using Citrus framework with **shared CamelContext**.

### Test Setup: Shared CamelContext

The most important feature of this test is sharing the CamelContext between Quarkus, Apache Camel, and Citrus:

```java
@Inject
@BindToRegistry
CamelContext camelContext;
```

**What's happening here:**

- `@Inject`: Quarkus injects the CamelContext that manages all Camel routes in the application
- `@BindToRegistry`: Citrus annotation that registers this CamelContext in the Citrus context, making it available to Citrus Camel endpoints
- The same CamelContext instance is shared by:
  - Quarkus for lifecycle management
  - Apache Camel for route execution
  - Citrus for sending messages to routes and accessing mock endpoints
- This enables **direct route invocation** without external message brokers

### Test Setup: CamelQuarkusTestSupport

The test extends `CamelQuarkusTestSupport` to access Camel testing utilities:

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {
    // ...
}
```

**What this provides:**

- `getMockEndpoint(String uri)`: Access to Camel mock endpoints for verification
- Camel test lifecycle integration with Quarkus
- Additional Camel testing helpers and utilities
- Seamless integration between Camel's test support and Citrus framework

### Test Execution: Citrus Sends to Camel Route

The test uses Citrus's dynamic `camel:` endpoint URI to send messages directly to the Camel route:

```java
@Test
void shouldHandleEvents() {
    MockEndpoint mockEndpoint = getMockEndpoint("mock:words-out");
    mockEndpoint.expectedBodiesReceived(">> HOWDY");

    runner.when(
        send()
            .fork(true)
            .endpoint("camel:direct:words-in")
            .message()
            .body("Howdy")
    );

    runner.then(
        context -> {
            try {
                mockEndpoint.assertIsSatisfied();
            } catch (InterruptedException e) {
                throw new CitrusRuntimeException("Failed to verify mock endpoint", e);
            }
        }
    );
}
```

**Test Flow Breakdown:**

1. **Setup**: Get reference to the mock endpoint `mock:words-out` from the shared CamelContext
2. **Expectation**: Set up expected message using Camel's MockEndpoint API (`expectedBodiesReceived(">> HOWDY")`)
3. **WHEN**: Send a message to the Camel route using Citrus
   - `endpoint("camel:direct:words-in")`: Dynamic Citrus endpoint URI that resolves to the Camel direct endpoint
   - `.fork(true)`: Send asynchronously (required for direct endpoints to avoid blocking)
   - The message triggers the Camel route synchronously
4. **THEN**: Verify the mock endpoint received the expected message
   - Uses Camel's `assertIsSatisfied()` method
   - Validates both message content and count

### Key Testing Pattern: Citrus + Camel MockEndpoint

This example demonstrates a powerful testing pattern combining:

- **Citrus**: For test orchestration, message sending, and Given-When-Then structure
- **Camel MockEndpoint**: For detailed message verification and Camel-specific assertions

This hybrid approach gives you:
- Citrus's readable test DSL and multi-protocol support
- Camel's powerful MockEndpoint assertion capabilities
- Direct access to Camel internals for deep integration testing

## Key Testing Concepts

### 1. Shared CamelContext Integration

The shared CamelContext is the cornerstone of this testing approach:

```java
@Inject
@BindToRegistry
CamelContext camelContext;
```

**Benefits:**

- No need for external message brokers or test containers
- Direct route invocation for fast test execution
- Access to all Camel components and endpoints
- Ability to inspect and verify Camel internals
- Single CamelContext instance ensures consistent state

### 2. Citrus Camel Module

The `citrus-camel` Maven dependency is added to the Maven POM:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-camel</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module adds:
- Camel endpoint support in Citrus (dynamic `camel:` URI scheme)
- CamelContext integration with Citrus registry
- Seamless message exchange conversion between Citrus and Camel
- Support for all Camel components as Citrus endpoints

### 3. Camel Quarkus JUnit Support

The `camel-quarkus-junit` dependency provides testing utilities:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-junit</artifactId>
    <scope>test</scope>
</dependency>
```

This provides:
- `CamelQuarkusTestSupport` base class
- `getMockEndpoint()` and other helper methods
- Integration between Camel's test framework and Quarkus
- MockEndpoint assertion capabilities

### 4. Test Annotations

The test class uses several key annotations to integrate Quarkus, Camel, and Citrus:

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;
    
    // ... test methods
}
```

**Annotation breakdown:**

- `@QuarkusTest`: Starts the Quarkus application (including Camel routes) in test mode
- `@CitrusSupport`: Enables Citrus framework integration with Quarkus
- `extends CamelQuarkusTestSupport`: Provides access to Camel testing utilities like `getMockEndpoint()`
- `@BindToRegistry`: Registers the CamelContext in Citrus's context, enabling `camel:` endpoint URIs
- `@CitrusResource`: Injects the Citrus test runner for executing test actions with Given-When-Then syntax
- `TestActionSupport`: Interface that provides convenient static imports for test actions like `send()`, `receive()`, etc.

### 5. Dynamic Camel Endpoint URIs

Citrus supports dynamic Camel endpoint URIs in the format `camel:{component}:{endpoint}`:

```java
send()
    .endpoint("camel:direct:words-in")
    .message()
    .body("Howdy")
```

**How it works:**

1. Citrus parses the `camel:` URI scheme
2. Looks up the CamelContext from the registry (registered via `@BindToRegistry`)
3. Resolves `direct:words-in` as a Camel endpoint in that context
4. Sends the Citrus message to the Camel endpoint
5. Citrus message is automatically converted to a Camel Exchange

This allows you to test **any** Camel component (`direct:`, `jms:`, `http:`, `kafka:`, etc.) using the same pattern.

### 6. Fork Mode for Synchronous Routes

The test uses `.fork(true)` when sending to the direct endpoint:

```java
send()
    .fork(true)
    .endpoint("camel:direct:words-in")
```

**Why fork is needed:**

- The `direct:` component executes routes **synchronously** in the caller's thread
- Without `fork(true)`, the Citrus send action would block until the route completes
- The route sends to a mock endpoint, but the verification happens later in the test
- `fork(true)` sends the message in a separate thread, allowing the test to proceed
- This pattern is specific to testing synchronous Camel routes with asynchronous verification

For asynchronous Camel routes (e.g., `seda:`, `jms:`), fork mode is typically not needed.

### 7. No External Dependencies

This testing approach requires **zero external infrastructure**:

- No JMS broker (unlike the camel-jms example)
- No Kafka cluster (unlike the event-driven-kafka example)
- No HTTP server
- No database
- No test containers

Everything runs in-memory within a single JVM, making tests:
- **Fast**: No container startup overhead
- **Isolated**: No shared state between tests
- **Portable**: No Docker or external services required
- **Deterministic**: No timing issues or network flakiness

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-direct/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Apache Camel routes are discovered and started
3. The CamelContext is created and injected into the test
4. Citrus registers the CamelContext for use by `camel:` endpoints
5. The test sets expectations on the mock endpoint
6. Citrus sends a message to the `direct:words-in` endpoint
7. The Camel route executes synchronously: consumes → transforms → sends to mock
8. The test verifies the mock endpoint received the expected message
9. The test completes

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Execution time**: Typically under 2 seconds (no container startup needed)

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-junit**: Camel test support for Quarkus (provides `CamelQuarkusTestSupport`)
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-camel**: Adds Camel endpoint support to Citrus (enables `camel:` URI scheme)
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

**Note**: This example has minimal dependencies—no JMS, Kafka, or other external transports needed.

## Comparison with Other Testing Approaches

### vs. camel-jms Example
- **camel-jms**: Tests Camel routes via external JMS broker (realistic but slower)
- **camel-direct**: Tests routes directly in-memory (fast but requires mock endpoints)

### vs. event-driven Examples
- **event-driven-***: Tests application behavior via external protocols (JMS, Kafka)
- **camel-direct**: Tests Camel route logic directly without external systems

### When to Use This Approach

**Use camel-direct testing when:**
- Testing route transformation logic in isolation
- Developing routes before external systems are ready
- You need fast, repeatable tests without external dependencies
- Testing complex routing logic, content-based routing, or EIP patterns
- Building a test suite that runs in CI/CD without Docker

**Use external endpoint testing (camel-jms, event-driven-*) when:**
- Testing end-to-end integration with real systems
- Validating protocol-specific behavior (JMS transactions, Kafka offsets, etc.)
- You need to verify the actual deployment configuration
- Testing production-like scenarios

**Best practice**: Use both! Direct testing for fast feedback on route logic, external testing for integration validation.

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Citrus Camel Module](https://citrusframework.org/docs/endpoints/camel/) - Camel endpoint reference for Citrus
- [Apache Camel Quarkus Core Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html) - Camel core functionality
- [Apache Camel Direct Component](https://camel.apache.org/components/latest/direct-component.html) - Direct component documentation
- [Apache Camel Mock Component](https://camel.apache.org/components/latest/mock-component.html) - Mock component documentation
- [Apache Camel Testing](https://camel.apache.org/manual/testing.html) - Official Camel testing guide
- [Apache Camel Documentation](https://camel.apache.org/) - Official Camel documentation

---

**Next Steps**: Try adding more complex Camel patterns like content-based routing (`choice().when()`), message filtering, splitting/aggregation, or error handling. Explore testing multiple interconnected routes using `direct:` endpoints. Learn about Camel's `adviceWith()` for replacing route endpoints in tests.

## Sample contract

**Purpose**: This example demonstrates how to test Apache Camel routes using Citrus framework with **shared CamelContext integration**.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-camel`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Camel test actions.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-direct/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-direct /tmp/citrus-camel-direct
cd /tmp/citrus-camel-direct
mvn clean verify -Dsystem.under.test.mode=embedded
```
