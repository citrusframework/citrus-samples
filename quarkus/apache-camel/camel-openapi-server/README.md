# OpenAPI Server Integration Testing with Citrus

This example demonstrates how to test an HTTP REST service implemented via Apache Camel OpenAPI integration using the Citrus framework. The REST service exposes a Petstore API defined in an OpenAPI 3.0 specification. Citrus acts as an OpenAPI-aware HTTP client that automatically generates requests and validates responses based on the specification.

## What You'll Learn

By the end of this guide, you'll understand:

- How to implement a REST service using Camel's OpenAPI integration in Quarkus
- How to auto-generate REST endpoints from an OpenAPI specification
- How to use Citrus OpenAPI client actions to test REST API endpoints
- How to validate responses against OpenAPI schema definitions
- How to handle path parameters, request bodies, and different HTTP status codes
- How to test multiple operations (GET, POST, PUT, DELETE) using operation IDs

## The Application Under Test

The application is a Petstore REST API built with Apache Camel and Quarkus. Instead of manually defining REST endpoints, the application uses Camel's `rest().openApi()` DSL to automatically generate routes from a `petstore-api.json` OpenAPI specification.

### Architecture

The `PetstoreRoutes` class configures the REST service:

```java
restConfiguration()
        .clientRequestValidation(true)
        .apiContextPath("openapi");

rest()
        .openApi()
        .specification("petstore-api.json")
        .missingOperation("mock");
```

Key aspects:
- **Client request validation** ensures incoming requests conform to the OpenAPI schema
- **API context path** exposes the OpenAPI specification at `/openapi` for runtime discovery
- **Missing operation mock** automatically generates mock responses from classpath resources (`examples/pet/1000.json`)

The API supports four operations:
- `GET /petstore/pet/{petId}` - Find a pet by ID (`getPetById`)
- `POST /petstore/pet` - Add a new pet (`addPet`)
- `PUT /petstore/pet` - Update an existing pet (`updatePet`)
- `DELETE /petstore/pet/{petId}` - Delete a pet (`deletePet`)

## Understanding the Citrus OpenAPI Test

The test class `PetstoreOpenApiTest` uses Citrus's OpenAPI integration to interact with the API. Instead of manually constructing HTTP requests, the test references operations by their OpenAPI `operationId`.

### Test Setup

```java
@QuarkusTest
@CitrusSupport
class PetstoreOpenApiTest {

    @CitrusResource
    TestCaseRunner t;

    OpenApiSpecification petstoreApi = OpenApiSpecification.from("http://localhost:8081/openapi");
}
```

The `OpenApiSpecification` is loaded from the running application's `/openapi` endpoint. This ensures the test always uses the same API contract as the server.

### Testing GET - Find Pet by ID

```java
@Test
void shouldGetPetById() {
    t.given(
        createVariables()
            .variable("petId", "1000")
    );

    t.when(
        openapi().specification(petstoreApi)
                .client("http://localhost:8081")
                .send("getPetById")
    );

    t.then(
        openapi().specification(petstoreApi)
                .client("http://localhost:8081")
                .receive("getPetById", HttpStatus.OK)
    );
}
```

Citrus automatically resolves the `petId` path parameter from the test variable, constructs the correct URL (`/petstore/pet/1000`), and validates the response against the Pet schema.

### Testing POST - Add a Pet

```java
t.when(
    openapi().specification(petstoreApi)
            .client("http://localhost:8081")
            .send("addPet")
);

t.then(
    openapi().specification(petstoreApi)
            .client("http://localhost:8081")
            .receive("addPet", HttpStatus.CREATED)
);
```

Citrus generates a valid Pet request body from the schema definition and sends it as a POST request.

### Testing PUT and DELETE

The `updatePet` and `deletePet` operations follow the same pattern, using their respective operation IDs. Since the Camel mock operation returns 200 OK for all operations by default, the tests validate against `HttpStatus.OK`.

## Key Testing Concepts

1. **Specification-driven testing** - The OpenAPI spec drives both the server implementation and the test client, ensuring the API contract is verified end-to-end.

2. **Operation-based actions** - Instead of hardcoding URLs and payloads, tests reference operations by `operationId` (e.g., `getPetById`, `addPet`). Citrus resolves paths, parameters, and request bodies from the spec.

3. **Automatic request generation** - Citrus generates valid request payloads from the OpenAPI schema definitions, including required fields and proper data types.

4. **Schema-based validation** - Responses are validated against the schema defined in the OpenAPI specification, catching contract violations automatically.

5. **Variable resolution** - Path parameters like `petId` are resolved from Citrus test variables, making tests data-driven and reusable.

6. **Runtime spec discovery** - Loading the spec from the running application (`http://localhost:8081/openapi`) ensures tests always match the deployed API version.

## Running the Tests

```bash
./mvnw -f quarkus/apache-camel/camel-openapi-server/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Expected output:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Key Dependencies (pom.xml)

- **camel-quarkus-rest** - Camel REST DSL support
- **camel-quarkus-platform-http** - HTTP transport for REST endpoints
- **camel-quarkus-jackson** - JSON marshalling/unmarshalling
- **camel-quarkus-openapi-java** - Serves the OpenAPI specification at runtime
- **camel-quarkus-rest-openapi** - Camel REST OpenAPI integration for auto-generating routes
- **citrus-quarkus** - Citrus integration with Quarkus test framework
- **citrus-http** - HTTP client/server support for Citrus
- **citrus-openapi** - OpenAPI specification-driven test actions
- **citrus-validation-json** - JSON response body validation

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/citrus/reference/html/)
- [Apache Camel REST DSL](https://camel.apache.org/components/latest/others/rest-openapi.html)
- [Quarkus Camel Extension](https://camel.apache.org/camel-quarkus/latest/)
- [OpenAPI Specification](https://swagger.io/specification/)
