# OpenAPI Client Integration Testing with Citrus

This example demonstrates how to test a Quarkus application that uses the **Apache Camel OpenAPI REST client** to call an external HTTP service, while **Citrus acts as the simulated HTTP server** using the same OpenAPI specification.

This is the reverse of the `camel-openapi-server` example: there, Citrus is an OpenAPI client testing a Camel server. Here, Citrus is an OpenAPI server simulating the external service that the Camel client depends on.

## What You'll Learn

By the end of this guide, you'll understand:

- How to implement an HTTP client in Camel using the `rest-openapi` component
- How Camel resolves path parameters and serialises request bodies from an OpenAPI spec
- How to share a single Citrus HTTP server endpoint across multiple test classes via `@CitrusConfiguration`
- How to stop the shared Citrus HTTP server after all tests using an `AfterSuite` action
- The difference between **explicit HTTP validation** (`PetstoreOpenApiClientTest`) and **OpenAPI spec-driven validation** (`PetstoreOpenApiSpecTest`)
- How to trigger Camel routes from a Citrus test using the Camel `send()` test action with `.fork(true)`

## The Application Under Test

The application is a Petstore API **client** built with Apache Camel and Quarkus. It exposes four internal Camel routes (triggered via `direct:` endpoints) that each invoke a specific Petstore API operation against a configurable external service URL.

### Architecture

The [`PetstoreClientRoutes`](src/main/java/org/citrusframework/samples/quarkus/camel/openapi/client/PetstoreClientRoutes.java) class defines the outbound routes:

```java
from("direct:getPetById")
        .to("rest-openapi:petstore-api.json#getPetById?host={{petstore.service.url}}");

from("direct:addPet")
        .marshal().json()
        .to("rest-openapi:petstore-api.json#addPet?host={{petstore.service.url}}");

from("direct:updatePet")
        .marshal().json()
        .to("rest-openapi:petstore-api.json#updatePet?host={{petstore.service.url}}");

from("direct:deletePet")
        .to("rest-openapi:petstore-api.json#deletePet?host={{petstore.service.url}}");
```

Key aspects:
- **`rest-openapi` component** reads `petstore-api.json` from the classpath and maps each operation ID to the correct HTTP method, path, and parameters
- **`{{petstore.service.url}}`** is a Camel property placeholder resolved from `application.properties` — the test profile overrides it to point to the Citrus-managed HTTP server
- **`marshal().json()`** serialises the Java `Map` body to JSON before POST/PUT requests are sent; the `camel-quarkus-jackson` extension provides the data format

### Application Properties

```properties
# src/main/resources/application.properties
petstore.service.url=http://petstore.prod-service.local
%test.petstore.service.url=http://localhost:18080
```

The `%test.` prefix activates Quarkus's `test` profile, so the Citrus test server URL is injected automatically during tests without changing application code.

## Shared Endpoint Configuration

Both test classes share a single Citrus HTTP server instance, defined in [`CitrusEndpointConfig`](src/test/java/org/citrusframework/samples/quarkus/camel/openapi/client/CitrusEndpointConfig.java):

```java
@CitrusConfiguration
public class CitrusEndpointConfig {

    HttpServer petstoreServer;

    @BindToRegistry
    public HttpServer petstoreServer() {
        if (petstoreServer == null) {
            petstoreServer = new HttpServerBuilder()
                .port(18080)
                .autoStart(true)
                .timeout(10000)
                .build();
        }
        return petstoreServer;
    }

    @BindToRegistry
    public AfterSuite afterSuiteActions() {
        return afterSuite()
                .actions(actions.stop(petstoreServer()))
                .build();
    }
}
```

Key aspects:
- **Lazy singleton** — the server is created once and reused across both test classes, avoiding "Address already in use" errors
- **`AfterSuite` action** — the server is stopped after all test suites finish, which is the correct lifecycle hook for shared infrastructure
- Both tests reference the server with `@CitrusEndpoint HttpServer petstoreServer;` and `@CitrusConfiguration(classes = CitrusEndpointConfig.class)`

## Triggering Camel Routes from Tests

Both tests use the Citrus Camel `send()` action with `.fork(true)` instead of a raw `ProducerTemplate`:

```java
runner.when(
    camel().send()
            .endpoint(CamelSupport.camel().endpoint(direct("getPetById")::getRawUri))
            .fork(true)
            .message().header("petId", "${petId}")
);
```

- **`.fork(true)`** runs the Camel route invocation on a separate thread, which is required because the Camel HTTP producer blocks waiting for a response while the Citrus main thread must proceed to handle the incoming server request
- **`direct("getPetById")::getRawUri`** uses the Camel Endpoint DSL for a type-safe, IDE-friendly reference to the route's entry point

## Two Test Styles

The module contains two test classes that demonstrate different Citrus verification styles for the same four operations.

### `PetstoreOpenApiClientTest` — Explicit HTTP Validation

[`PetstoreOpenApiClientTest`](src/test/java/org/citrusframework/samples/quarkus/camel/openapi/client/PetstoreOpenApiClientTest.java) uses plain `http()` server actions to verify exact request paths, methods, bodies, and content types, and provides explicit simulated response bodies.

```java
// Trigger the Camel route
runner.when(
    camel().send()
            .endpoint(CamelSupport.camel().endpoint(direct("getPetById")::getRawUri))
            .fork(true)
            .message().header("petId", "${petId}")
);

// Verify the exact incoming request
runner.then(
    http()
        .server(petstoreServer)
        .receive()
        .get("/pet/${petId}")
        .message()
);

// Send back an explicit simulated response
runner.then(
    http()
        .server(petstoreServer)
        .send()
        .response(HttpStatus.OK)
        .message()
        .body(new DefaultPayloadBuilder(newPet(1000, "hasso", "dog", 1L, "available")))
        .process(processor().camel().marshal().json())
);
```

For POST and PUT, the `receive()` action also validates the request body and content type:

```java
runner.then(
    http()
        .server(petstoreServer)
        .receive()
        .post("/pet")
        .message()
        .body(new ObjectMappingPayloadBuilder(petBody))
        .contentType("application/json;charset=UTF-8")
);
```

This style is suited for tests where you want to assert the exact wire format of the HTTP traffic — specific field values, headers, and response payloads.

A custom `ObjectMapper` is registered via `@BindToRegistry` to control JSON serialisation behaviour (non-empty fields, enum-as-string, pretty-print) when building and matching request/response bodies.

### `PetstoreOpenApiSpecTest` — OpenAPI Spec-Driven Validation

[`PetstoreOpenApiSpecTest`](src/test/java/org/citrusframework/samples/quarkus/camel/openapi/client/PetstoreOpenApiSpecTest.java) uses Citrus's `openapi()` server actions, which automatically derive the expected request and a generated response from the OpenAPI specification.

```java
runner.when(
    camel().send()
            .endpoint(CamelSupport.camel().endpoint(direct("getPetById")::getRawUri))
            .fork(true)
            .message().header("petId", "${petId}")
);

// Citrus validates the request against the OpenAPI spec
runner.then(
    openapi().specification(petstoreApi)
            .server(petstoreServer)
            .receive("getPetById")
);

// Citrus generates a spec-compliant response
runner.then(
    openapi().specification(petstoreApi)
            .server(petstoreServer)
            .send("getPetById", HttpStatus.OK)
);
```

This style is suited for contract-compliance tests: you care that the Camel client sends a request that conforms to the spec, and you want realistic but schema-generated responses without crafting them manually.

## Running the Tests

```bash
cd apache-camel/camel-openapi-client
./mvnw -f quarkus/apache-camel/camel-openapi-client/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Expected output:
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

## Key Dependencies (pom.xml)

- **camel-quarkus-rest** — Camel REST DSL support
- **camel-quarkus-platform-http** — HTTP transport
- **camel-quarkus-jackson** — JSON marshalling via `marshal().json()`
- **camel-quarkus-rest-openapi** — OpenAPI-driven REST client (`rest-openapi:` component)
- **camel-quarkus-http** — HTTP producer used by `rest-openapi` to make outbound calls
- **citrus-quarkus** — Citrus integration with the Quarkus test framework
- **citrus-camel** — Citrus Camel test actions (`camel().send()`)
- **citrus-http** — HTTP server support for Citrus (Jetty-based)
- **citrus-openapi** — OpenAPI specification-driven test actions (`openapi().server()`)
- **citrus-validation-json** — JSON request/response body validation
- **citrus-validation-text** — Plain-text validation context required for GET/DELETE (bodyless) operations

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/citrus/reference/html/)
- [Apache Camel REST OpenAPI Component](https://camel.apache.org/components/latest/rest-openapi-component.html)
- [Quarkus Camel Extension](https://camel.apache.org/camel-quarkus/latest/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [camel-openapi-server example](../camel-openapi-server/README.md) — the complementary server-side example
