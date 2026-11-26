# Apache Camel HTTP Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes that call external HTTP services using Citrus framework with **simulated HTTP server** and **shared CamelContext integration**. The project showcases a translation service integration where Citrus provides full control over HTTP request validation and response simulation.

## What You'll Learn

By the end of this guide, you'll understand:

- How to create Apache Camel routes that call external HTTP services
- How to use Citrus HTTP server to simulate external service dependencies
- How to share the CamelContext between Quarkus, Camel, and Citrus
- How to validate HTTP requests made by Camel routes (method, URL, query parameters, body)
- How to simulate HTTP responses with full control over status codes and content
- How to test complex Camel routing logic with content-based routing and variables
- How to combine direct endpoints, HTTP calls, and mock endpoints in a single test
- The benefits of service virtualization for testing integration scenarios

## The Application Under Test

The Quarkus application uses Apache Camel to implement an integration route that calls an external HTTP translation service:

```
Direct Endpoint (direct:words-in) → Translation Route → HTTP Service → Transform → Mock Endpoint (mock:words-out)
```

### Apache Camel Routes

The application consists of two interconnected Camel routes defined in `Routes.java`:

#### Main Route: Message Processing Pipeline

```java
from("direct:words-in")
    .to("direct:translate")
    .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
    .to("mock:words-out");
```

**Route Breakdown:**

1. **from("direct:words-in")**: Entry point for incoming messages
2. **to("direct:translate")**: Delegate to translation sub-route
3. **.setBody(...)**: Transform the translated result to uppercase with prefix
4. **to("mock:words-out")**: Send final result to mock endpoint for verification

#### Translation Route: HTTP Service Integration

```java
from("direct:translate")
    .choice()
        .when(simple("${header.lang} != null"))
            .setVariable("lang", simple("${header.lang}"))
            .removeHeaders("*")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.HTTP_QUERY, simple("lang=${variable.lang}"))
            .to("http://{{camel.translate.service.host}}:{{camel.translate.service.port}}/translate")
            .convertBodyTo(String.class)
        .otherwise()
            .setBody(simple("${body}"));
```

**Route Breakdown:**

1. **from("direct:translate")**: Sub-route for translation logic
2. **choice().when(...)**: Content-based routing (CBR) pattern
   - If `lang` header is present, call translation service
   - Otherwise, return original message unchanged
3. **setVariable("lang", ...)**: Store language in Camel variable for later use
4. **removeHeaders("*")**: Clear all headers before HTTP call (prevents header pollution)
5. **setHeader(Exchange.HTTP_METHOD, constant("POST"))**: Configure POST request
6. **setHeader(Exchange.HTTP_QUERY, ...)**: Set query parameter from variable
7. **to("http://...")**: Call external HTTP service
   - Uses property placeholders for host and port configuration
   - URL: `http://localhost:9001/translate?lang=us-texas`
8. **convertBodyTo(String.class)**: Convert HTTP response body to String

### Understanding the Architecture

This example demonstrates several important patterns:

**1. Route Composition**: Breaking complex logic into smaller, reusable routes
- Main route handles orchestration
- Translation route handles HTTP integration
- Clear separation of concerns

**2. Content-Based Routing (CBR)**: Conditional logic based on message content
- Different processing paths based on presence of `lang` header
- Enterprise Integration Pattern for routing decisions

**3. Variable vs. Header Management**:
- Store `lang` in variable before clearing headers
- Prevents header from being sent to HTTP service
- Variables survive header removal

**4. Property-Based Configuration**:
- HTTP endpoint configured via `application.properties`
- Easy to change for different environments
- Test configuration can override production values

**5. External Service Integration**:
- HTTP component for REST API calls
- Realistic integration scenario
- Perfect candidate for service virtualization in tests

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to test Camel routes with external HTTP dependencies using Citrus's HTTP server simulation.

### Test Setup: Shared CamelContext

As with the camel-direct example, this test shares the CamelContext:

```java
@Inject
@BindToRegistry
CamelContext camelContext;
```

This enables:
- Direct invocation of Camel routes via `camel:direct:words-in`
- Access to mock endpoints for verification
- Single CamelContext instance across Quarkus, Camel, and Citrus

### Test Setup: Citrus HTTP Server

The key feature of this test is the simulated HTTP server:

```java
@CitrusEndpoint
@HttpServerConfig(autoStart = true, port = 9001)
HttpServer translateServer;
```

**What's happening here:**

- `@CitrusEndpoint`: Marks the field as a Citrus endpoint (automatically configured)
- `@HttpServerConfig`: Configures the HTTP server
  - `autoStart = true`: Server starts automatically before tests run
  - `port = 9001`: Must match the port in `application.properties` (`camel.translate.service.port=9001`)
- **translateServer**: Citrus HTTP server that simulates the translation service
- The Camel route will make real HTTP calls to this server during the test

### Test Setup: Configuration Alignment

The test configuration ensures the Camel route calls the Citrus HTTP server:

**application.properties:**
```properties
camel.translate.service.host=localhost
camel.translate.service.port=9001
```

**Citrus HTTP Server:**
```java
@HttpServerConfig(autoStart = true, port = 9001)
```

Both configurations use **port 9001**, ensuring the Camel HTTP call reaches the Citrus server.

### Test Execution: Full Integration Flow

The test orchestrates a complete integration scenario with HTTP service simulation:

```java
@Test
void shouldHandleEvents() {
    // Step 1: Setup expectation on mock endpoint
    MockEndpoint mockEndpoint = getMockEndpoint("mock:words-out");
    mockEndpoint.expectedBodiesReceived(">> HOWDY");

    // Step 2: Send message to Camel route
    runner.when(
        send()
            .fork(true)
            .endpoint("camel:direct:words-in")
            .message()
            .header("lang", "us-texas")
            .body("Hello")
    );

    // Step 3: Receive and validate HTTP request from Camel
    runner.when(
        http().server(translateServer)
            .receive()
            .post("/translate")
            .message()
            .queryParam("lang", "us-texas")
            .body("Hello")
    );

    // Step 4: Simulate HTTP response
    runner.then(
        http().server(translateServer)
                .send()
                .response(200)
                .message()
                .body("Howdy")
    );

    // Step 5: Verify final result on mock endpoint
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

1. **Setup**: Configure Camel mock endpoint to expect `">> HOWDY"`

2. **Trigger**: Send message to Camel route via `camel:direct:words-in`
   - Header: `lang=us-texas`
   - Body: `"Hello"`
   - `.fork(true)`: Asynchronous send (required for synchronous direct endpoints)

3. **Validate HTTP Request**: Citrus HTTP server receives the request from Camel
   - **Method**: `POST`
   - **Path**: `/translate`
   - **Query Parameter**: `lang=us-texas` (from Camel variable)
   - **Body**: `"Hello"` (original message)
   - This validates that Camel correctly prepared the HTTP request

4. **Simulate HTTP Response**: Citrus sends back a response
   - **Status Code**: `200 OK`
   - **Body**: `"Howdy"` (translated text)
   - This simulates the external translation service

5. **Verify Final Result**: Check that mock endpoint received the processed message
   - Expected: `">> HOWDY"` (response transformed to uppercase with prefix)

### The Power of HTTP Service Simulation

This test demonstrates **service virtualization** with complete control:

**Request Validation:**
- HTTP method (POST, GET, PUT, DELETE, etc.)
- URL path (`/translate`)
- Query parameters (`lang=us-texas`)
- Request body (`"Hello"`)
- Headers (if needed)
- Content types

**Response Simulation:**
- HTTP status codes (200, 404, 500, etc.)
- Response body with any content
- Response headers
- Delays (to test timeouts)
- Errors (to test error handling)

**Benefits:**
- No need for real external services
- Full control over responses (including edge cases)
- Deterministic testing (no flaky external dependencies)
- Fast execution (no network latency)
- Test error scenarios that are hard to trigger in real services

## Key Testing Concepts

### 1. Service Virtualization

Citrus HTTP server acts as a **test double** for the external translation service:

```java
@CitrusEndpoint
@HttpServerConfig(autoStart = true, port = 9001)
HttpServer translateServer;
```

**Why this matters:**

- **Isolation**: Tests don't depend on external services
- **Control**: Simulate any response scenario (success, errors, edge cases)
- **Speed**: No network calls to real services
- **Reliability**: No flaky tests due to external service downtime
- **Early Testing**: Test integration before the real service exists

### 2. HTTP Request/Response Validation

Citrus provides rich HTTP testing capabilities:

**Receive and validate HTTP request:**
```java
http().server(translateServer)
    .receive()
    .post("/translate")
    .message()
    .queryParam("lang", "us-texas")
    .body("Hello")
```

**Send HTTP response:**
```java
http().server(translateServer)
    .send()
    .response(200)
    .message()
    .body("Howdy")
```

This pattern allows you to:
- Verify that Camel routes make correct HTTP calls
- Simulate various service behaviors
- Test error handling and retry logic
- Validate request transformation logic

### 3. Citrus HTTP Module

The `citrus-http` Maven dependency is added to the Maven POM:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-http</artifactId>
    <scope>test</scope>
</dependency>
```

This module adds:
- HTTP server endpoint for service simulation
- HTTP client endpoint for calling services
- Rich request/response validation DSL
- Support for REST, SOAP, and plain HTTP
- JSON and XML validation capabilities

### 4. Camel HTTP Component

The application uses the Camel HTTP component:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-http</artifactId>
</dependency>
```

This provides:
- HTTP client integration using Apache HttpClient 5.x
- Support for HTTP methods (GET, POST, PUT, DELETE, etc.)
- Query parameter and header handling
- Request/response body transformation
- Connection pooling and timeout configuration

### 5. Content-Based Routing (CBR)

The translation route demonstrates the CBR pattern:

```java
.choice()
    .when(simple("${header.lang} != null"))
        // Call translation service
    .otherwise()
        // Return original message
```

**Testing implications:**

- Test both branches of the conditional logic
- Verify correct HTTP call only when `lang` header is present
- Validate fallback behavior when header is absent
- This example tests the translation path; you could add a test for the otherwise path

### 6. Camel Variables vs. Headers

The route uses variables to preserve data across header operations:

```java
.setVariable("lang", simple("${header.lang}"))
.removeHeaders("*")
.setHeader(Exchange.HTTP_QUERY, simple("lang=${variable.lang}"))
```

**Why this pattern:**

- Headers might pollute the HTTP request
- Variables survive `removeHeaders("*")`
- Clean separation between routing metadata and message content
- Best practice for complex route transformations

### 7. Property Placeholders

The HTTP endpoint uses property placeholders:

```java
.to("http://{{camel.translate.service.host}}:{{camel.translate.service.port}}/translate")
```

**Configuration:**
```properties
camel.translate.service.host=localhost
camel.translate.service.port=9001
```

**Benefits:**

- Environment-specific configuration
- Easy to override for testing
- No hardcoded URLs in routes
- Supports different environments (dev, test, prod)

### 8. Test Annotations

The test class combines multiple frameworks:

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = true, port = 9001)
    HttpServer translateServer;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

**Annotation breakdown:**

- `@QuarkusTest`: Starts Quarkus application with Camel routes
- `@CitrusSupport`: Enables Citrus framework integration
- `extends CamelQuarkusTestSupport`: Access to Camel test utilities (getMockEndpoint, etc.)
- `@BindToRegistry`: Shares CamelContext with Citrus
- `@CitrusEndpoint` + `@HttpServerConfig`: Configures HTTP server
- `@CitrusResource`: Injects Citrus test runner
- `TestActionSupport`: Provides static imports for test actions

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-direct-http/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Apache Camel routes are discovered and started
3. The CamelContext is created and injected into the test
4. Citrus registers the CamelContext for `camel:` endpoints
5. **Citrus HTTP server starts on port 9001** (before test execution)
6. The test sends a message to `direct:words-in` with `lang` header
7. Camel main route delegates to translation route
8. Translation route makes HTTP POST to `http://localhost:9001/translate?lang=us-texas`
9. **Citrus HTTP server receives the request** (validates method, path, query, body)
10. **Citrus HTTP server responds with `200 OK` and body `"Howdy"`**
11. Camel route receives the HTTP response
12. Main route transforms the result to `">> HOWDY"`
13. Main route sends to mock endpoint
14. Test verifies mock endpoint received expected message
15. Citrus HTTP server stops

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Execution time**: Typically under 3 seconds (HTTP server startup included)

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-http**: Apache Camel HTTP component (HTTP client)
- **camel-quarkus-junit**: Camel test support for Quarkus
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-camel**: Adds Camel endpoint support to Citrus (enables `camel:` URI scheme)
- **citrus-http**: Adds HTTP server/client support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Testing Scenarios

This pattern enables testing various scenarios:

### 1. Success Path (Current Test)
- Valid request with `lang` header
- Service returns successful response
- Result is transformed correctly

### 2. Error Handling (Extend the Test)
```java
// Simulate HTTP 500 error
http().server(translateServer)
    .send()
    .response(500)
    .message()
    .body("Service Unavailable");
```

### 3. Timeout Scenarios
```java
// Simulate slow service
http().server(translateServer)
    .send()
    .response(200)
    .message()
    .body("Howdy")
    .delay(5000); // 5 second delay
```

### 4. Missing Header Path
```java
// Send without lang header
send()
    .endpoint("camel:direct:words-in")
    .message()
    .body("Hello"); // No lang header

// No HTTP call should occur
// Mock endpoint should receive ">> HELLO" (original message uppercased)
```

### 5. Different Languages
```java
// Test different language codes
.header("lang", "fr")
.body("Hello")

// Expect query param: lang=fr
.queryParam("lang", "fr")

// Simulate French translation
.body("Bonjour")
```

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Citrus HTTP Module](https://citrusframework.org/docs/endpoints/http/) - HTTP endpoint reference for Citrus
- [Citrus Camel Module](https://citrusframework.org/docs/endpoints/camel/) - Camel endpoint reference for Citrus
- [Apache Camel Quarkus HTTP Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/http.html) - Camel HTTP component reference
- [Apache Camel Content-Based Router](https://camel.apache.org/components/latest/eips/choice-eip.html) - CBR pattern documentation
- [Apache Camel Testing](https://camel.apache.org/manual/testing.html) - Official Camel testing guide
- [Service Virtualization](https://en.wikipedia.org/wiki/Service_virtualization) - Understanding test doubles and service simulation

---

**Next Steps**: Try extending the test to cover error scenarios (HTTP 500, timeouts), test the otherwise branch (no `lang` header), or add more complex transformations. Explore Citrus's JSON and XML validation for testing REST APIs with structured data. Learn about Citrus's data dictionary feature for consistent test data management across multiple tests.

## Sample contract

**Purpose**: This example demonstrates how to test Apache Camel routes that call external HTTP services using Citrus framework with **simulated HTTP server** and **shared CamelContext integration**.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-camel`, `citrus-http`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Camel and HTTP actions.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-direct-http/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-direct-http /tmp/citrus-camel-direct-http
cd /tmp/citrus-camel-direct-http
mvn clean verify -Dsystem.under.test.mode=embedded
```
