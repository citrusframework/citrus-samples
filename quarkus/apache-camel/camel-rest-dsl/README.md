# Apache Camel REST DSL Integration Testing with Citrus

This example demonstrates how to test an HTTP REST service implemented via Apache Camel REST DSL using the Citrus framework. The REST service provides CRUD operations for a fruits API defined in an OpenAPI specification. Citrus acts as an HTTP client to validate the REST endpoints.

## What You'll Learn

By the end of this guide, you'll understand:

- How to implement a REST service using Apache Camel REST DSL in Quarkus
- How to define REST endpoints with GET, POST, and DELETE operations
- How to use Citrus HTTP client to test REST API endpoints
- How to validate JSON response bodies with Citrus
- How to test different HTTP status codes (200, 201, 204, 404)
- How to handle error cases like "not found" in Camel routes
- How to use JSON marshalling and unmarshalling in Camel routes
- How to use POJO-based testing with object mapping and `JsonMappingValidationProcessor`

## The Application Under Test

The Quarkus application uses Apache Camel REST DSL to implement a fruits REST API:

```
GET    /fruits  -> Returns list of all fruits (200)
POST   /fruits  -> Creates a new fruit (201)
DELETE /fruits  -> Deletes a fruit by name (204) or returns not found (404)
```

### OpenAPI Specification

The REST API follows an OpenAPI 3.0 specification located at `src/main/resources/openapi/fruits-api.yaml`. The specification defines:

- **Fruit schema**: An object with `name` and `description` string properties
- **GET /fruits**: Returns an array of Fruit entities (200)
- **POST /fruits**: Creates a new Fruit instance (201)
- **DELETE /fruits**: Deletes a Fruit instance (204) or returns 404 if not found

### Apache Camel REST DSL Routes

The application consists of REST DSL definitions and processing routes in `FruitRoutes.java`:

#### REST DSL Definition

```java
rest("/fruits")
        .get()
            .produces(APPLICATION_JSON_CONTENT_TYPE)
            .to("direct:get-fruits")
        .post()
            .consumes(APPLICATION_JSON_CONTENT_TYPE)
            .to("direct:create-fruit")
        .delete()
            .consumes(APPLICATION_JSON_CONTENT_TYPE)
            .to("direct:delete-fruit");
```

The REST DSL maps HTTP verbs to internal Camel `direct:` routes, separating the REST API definition from the processing logic.

#### Processing Routes

```java
from("direct:get-fruits")
        .setBody(exchange -> new ArrayList<>(fruits))
        .marshal().json();

from("direct:create-fruit")
        .unmarshal().json(Fruit.class)
        .process(exchange -> {
            Fruit fruit = exchange.getIn().getBody(Fruit.class);
            fruits.add(fruit);
            exchange.getIn().setBody(null);
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
        });

from("direct:delete-fruit")
        .unmarshal().json(Fruit.class)
        .process(exchange -> {
            Fruit fruit = exchange.getIn().getBody(Fruit.class);
            boolean found = fruits.removeIf(f -> f.getName().equals(fruit.getName()));
            exchange.getIn().setBody(null);
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, !found ? 404 : 204);
        });
```

Each route handles JSON marshalling/unmarshalling explicitly using Camel's Jackson data format and sets the appropriate HTTP response codes.

### Understanding the Architecture

**1. REST DSL**: Provides a clean, declarative way to define REST endpoints. The DSL maps HTTP verbs and paths to Camel routes without coupling the REST definition to the implementation.

**2. Direct Routes**: The REST DSL delegates to `direct:` endpoints, keeping the REST API definition separate from the business logic.

**3. JSON Processing**: Uses Camel's Jackson data format for `marshal().json()` (object to JSON) and `unmarshal().json(Fruit.class)` (JSON to object).

**4. In-Memory Store**: Fruits are stored in a `LinkedList` seeded with Apple, Mango, and Orange as initial data.

## Understanding the Citrus Test

There are two test classes demonstrating different testing styles:

- **`FruitRestDslTest`** — uses inline JSON strings for request/response validation
- **`FruitPojoRestDslTest`** — uses POJO-based testing with object mapping and `JsonMappingValidationProcessor`

### FruitRestDslTest

This test class demonstrates how to test Camel REST DSL endpoints using Citrus's HTTP client with inline JSON.

### Test Setup

```java
@QuarkusTest
@CitrusSupport
class FruitRestDslTest implements TestActionSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

- `@QuarkusTest`: Starts the Quarkus application with Camel routes on the test port (8081)
- `@CitrusSupport`: Enables Citrus framework integration
- `@CitrusEndpoint` + `@HttpClientConfig`: Configures a Citrus HTTP client pointing to the Quarkus test server
- `@CitrusResource`: Injects the Citrus test action runner

### Test: Create a Fruit (POST)

```java
@Test
void shouldAddFruit() {
    runner.when(
            http()
                    .client(fruitRestClient)
                    .send()
                    .post()
                    .path("/fruits")
                    .message()
                    .body("""
                    {
                       "name": "Pineapple",
                       "description": "A sweet and tasty tropical fruit."
                    }
                    """)
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
    );

    runner.then(
            http()
                    .client(fruitRestClient)
                    .receive()
                    .response(201)
    );
}
```

Sends a POST request with a JSON body and validates the 201 Created response.

### Test: List All Fruits (GET)

```java
@Test
void shouldListFruits() {
    runner.when(
            http()
                    .client(fruitRestClient)
                    .send()
                    .get()
                    .path("/fruits")
    );

    runner.then(
            http()
                    .client(fruitRestClient)
                    .receive()
                    .response(200)
                    .message()
                    .validate(validation().jsonPath()
                            .expression("$..name", "@contains(Apple,Mango,Orange)@"))
    );
}
```

Sends a GET request and validates the 200 OK response using a JsonPath expression. The `@contains()@` validation matcher verifies that the response contains all expected fruit names without requiring an exact body match.

### Test: Delete a Fruit (DELETE)

```java
@Test
void shouldDeleteFruit() {
    runner.when(
            http().client(fruitRestClient)
                    .send()
                    .delete("/fruits")
                    .message()
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .body("""
                    {
                        "name": "Apple"
                    }
                    """)
    );

    runner.then(
            http().client(fruitRestClient)
                    .receive()
                    .response(204)
    );
}
```

Sends a DELETE request with the fruit to remove and validates the 204 No Content response.

### Test: Delete a Non-Existing Fruit (DELETE 404)

```java
@Test
void shouldHandleFruitNotFound() {
    runner.when(
            http().client(fruitRestClient)
                    .send()
                    .delete("/fruits")
                    .message()
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .body("""
                    {
                        "name": "Pineapple",
                        "description": "A sweet and tasty tropical fruit."
                    }
                    """)
    );

    runner.then(
            http().client(fruitRestClient)
                    .receive()
                    .response(404)
    );
}
```

Sends a DELETE request for a fruit that does not exist in the store and validates the 404 Not Found response.

### FruitPojoRestDslTest

This test class demonstrates POJO-based testing with object mapping. Instead of inline JSON strings, it uses Java objects for both request bodies and response validation.

#### Test Setup with Object Mapper

```java
@QuarkusTest
@CitrusSupport
class FruitPojoRestDslTest implements TestActionSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();
}
```

The `@BindToRegistry` annotation registers a custom Jackson `ObjectMapper` with Citrus, which is then used for marshalling POJOs to JSON in requests and unmarshalling JSON responses back to POJOs.

#### POJO-Based Request Body

Instead of writing JSON strings, use `marshal()` to convert a Java object:

```java
runner.when(
        http()
                .client(fruitRestClient)
                .send()
                .post()
                .path("/fruits")
                .message()
                .body(marshal(new Fruit("Pineapple", "A sweet and tasty tropical fruit.")))
                .contentType(APPLICATION_JSON_CONTENT_TYPE)
);
```

#### POJO-Based Response Validation

Use `JsonMappingValidationProcessor` to deserialize the response into a typed object and validate it with standard Java assertions:

```java
runner.then(
        http()
                .client(fruitRestClient)
                .receive()
                .response(200)
                .message()
                .validate(new JsonMappingValidationProcessor<>(Fruit[].class) {
                    @Override
                    public void validate(Fruit[] fruits, Map<String, Object> headers, TestContext context) {
                        Assertions.assertTrue(fruits.length > 0);
                        Assertions.assertTrue(Arrays.stream(fruits)
                                .anyMatch(fruit -> fruit.getName().equals("Apple")));
                    }
                })
);
```

This approach gives you full type safety and the ability to use any Java assertion logic on the deserialized response.

## Key Testing Concepts

### 1. Citrus HTTP Client

Citrus provides a fluent DSL for HTTP client operations:

```java
@CitrusEndpoint
@HttpClientConfig(requestUrl = "http://localhost:8081")
HttpClient fruitRestClient;
```

The client is configured once and reused across tests to send requests and validate responses.

### 2. JSON Validation Approaches

Citrus offers multiple ways to validate JSON responses:

- **JsonPath validation**: Use `validation().jsonPath().expression(...)` with matchers like `@contains()@` to verify specific JSON fields without requiring an exact body match
- **Exact body match**: Provide the complete expected JSON in `.body(...)` for strict validation (via `citrus-validation-json`)
- **POJO-based validation**: Use `JsonMappingValidationProcessor` to deserialize the response into a typed Java object and validate with standard assertions

### 3. POJO Marshalling

Use `marshal()` from `org.citrusframework.dsl.JsonSupport` to convert Java objects to JSON for request bodies. Register a custom Jackson `ObjectMapper` with `@BindToRegistry` to control serialization behavior.

### 4. HTTP Status Code Validation

Each test validates the expected HTTP status code:
- `response(200)` for successful GET
- `response(201)` for successful POST (Created)
- `response(204)` for successful DELETE (No Content)
- `response(404)` for DELETE of a non-existing resource (Not Found)

### 5. Test Annotations

- `@QuarkusTest`: Starts the application in test mode on port 8081
- `@CitrusSupport`: Activates Citrus integration with the Quarkus test lifecycle
- `TestActionSupport`: Provides static imports for `http()`, `send()`, and other Citrus actions

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-rest-dsl/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode (port 8081)
2. Apache Camel REST DSL routes are discovered and started
3. Citrus HTTP client connects to the Quarkus test server
4. Each test sends HTTP requests and validates responses
5. Application shuts down after tests complete

**Expected output:**
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-rest**: Apache Camel REST DSL support
- **camel-quarkus-platform-http**: Uses Quarkus HTTP server as REST transport
- **camel-quarkus-jackson**: JSON marshalling/unmarshalling with Jackson
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-http**: Adds HTTP client/server support to Citrus
- **citrus-validation-json**: JSON-aware message validation
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus
- **jackson-annotations**: Jackson annotations for POJO-based JSON marshalling in tests

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Citrus HTTP Module](https://citrusframework.org/docs/endpoints/http/) - HTTP endpoint reference for Citrus
- [Apache Camel REST DSL](https://camel.apache.org/manual/rest-dsl.html) - Official Camel REST DSL documentation
- [Apache Camel Quarkus REST Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/rest.html) - Camel REST extension for Quarkus
- [OpenAPI Specification](https://swagger.io/specification/) - OpenAPI 3.0 specification reference

## Sample contract

**Purpose**: This example demonstrates how to test an HTTP REST service implemented via Apache Camel REST DSL using the Citrus framework.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-http`, `citrus-validation-json`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with HTTP and JSON validation.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-rest-dsl/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-rest-dsl /tmp/citrus-camel-rest-dsl
cd /tmp/citrus-camel-rest-dsl
mvn clean verify -Dsystem.under.test.mode=embedded
```
