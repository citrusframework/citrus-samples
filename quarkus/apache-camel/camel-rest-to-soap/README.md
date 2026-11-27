# Apache Camel REST to SOAP Bridge

This example demonstrates how to test a Camel REST-to-SOAP bridge using the Citrus framework. The Quarkus Camel application exposes an HTTP REST interface (JSON) and translates requests into SOAP operations on a backend WebService defined by a WSDL contract. Citrus acts as both the HTTP client calling the REST interface and the SOAP WebService server simulating the backend.

This builds on the [camel-cxf-soap-client](../camel-cxf-soap-client) example, which calls the same SOAP WebService via `direct` endpoints. Here, a Camel REST DSL facade is placed in front so external consumers interact with a JSON/HTTP API while the backend uses SOAP/XML.

## What You'll Learn

By the end of this guide, you'll understand:

- How to expose a REST API with the Camel REST DSL in Quarkus
- How to bridge REST requests to a SOAP backend using the CXF component
- How JSON-to-SOAP-XML and SOAP-XML-to-JSON transformation works via Camel REST binding mode and CXF POJO data format
- How to translate SOAP faults into HTTP error responses
- How to use WSDL2Java code generation for model objects and the service interface
- How to use the Citrus HTTP client to call the Camel REST interface
- How to use the Citrus SOAP server to simulate the backend WebService
- How to validate SOAP requests using raw XML strings or JAXB unmarshalling
- How to test SOAP fault handling end-to-end (SOAP fault -> HTTP 500 response)

## The Application Under Test

The Quarkus application uses the Camel REST DSL to expose HTTP endpoints that forward requests to a SOAP WebService via the CXF component:

```
HTTP GET    /fruits  -> direct:listFruits  -> CXF client -> SOAP listFruits
HTTP POST   /fruits  -> direct:addFruit    -> CXF client -> SOAP addFruit
HTTP DELETE /fruits  -> direct:deleteFruit -> CXF client -> SOAP deleteFruit
```

### WSDL Contract and WSDL2Java Code Generation

The service contract is defined in `src/main/resources/wsdl/FruitService.wsdl` (the same WSDL used in the other CXF examples). The project uses **WSDL2Java code generation** (via `quarkus-cxf-codegen`) to automatically generate all model objects and the service endpoint interface:

```properties
quarkus.cxf.codegen.wsdl2java.includes=wsdl/FruitService.wsdl
```

The generated classes include `Fruit`, `FruitService`, `AddFruit`, `AddFruitResponse`, `DeleteFruit`, `DeleteFruitResponse`, `ListFruits`, `ListFruitsResponse`, and `ObjectFactory`.

### REST DSL and CXF Client Routes

The `FruitClientRoutes` class configures the REST facade and the CXF client routes:

```java
restConfiguration()
        .bindingMode(RestBindingMode.json);

rest("/fruits").post()
        .type(Fruit.class)
        .produces(APPLICATION_JSON_CONTENT_TYPE)
        .to("direct:addFruit");
rest("/fruits").get()
        .type(Fruit.class)
        .produces(APPLICATION_JSON_CONTENT_TYPE)
        .to("direct:listFruits");
rest("/fruits").delete()
        .type(Fruit.class)
        .produces(APPLICATION_JSON_CONTENT_TYPE)
        .to("direct:deleteFruit");

from("direct:listFruits")
        .setHeader(CxfConstants.OPERATION_NAME, constant("listFruits"))
        .to("cxf:bean:fruitSoapClient");
```

Key aspects:
- **REST binding mode `json`** automatically marshals/unmarshals JSON request and response bodies using Jackson
- **REST DSL** defines HTTP verbs and paths that route to `direct` endpoints
- **CXF bean endpoint** as a producer (client) calls the external SOAP service
- **POJO data format** (CXF default) marshals Java objects to SOAP XML and unmarshals responses back
- **Operation name header** (`CxfConstants.OPERATION_NAME`) identifies which WSDL operation to invoke
- **Configurable address** — the external service URL is set via `fruit.service.url` property, allowing the test to point it to the Citrus SOAP server

### SOAP Fault to HTTP Error Translation

The route includes an exception handler that catches `SoapFault` exceptions from the CXF client and translates them into HTTP 500 responses:

```java
onException(SoapFault.class)
        .handled(true)
        .process(this::processSoapFault);

private void processSoapFault(Exchange exchange) {
    SoapFault caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SoapFault.class);
    exchange.getIn().setBody(caused.getMessage());
    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
}
```

### Understanding the Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│  Quarkus Application                                                │
│                                                                     │
│  HTTP REST API          Camel Routes         CXF SOAP Client        │
│  ┌─────────────┐   ┌─────────────────┐   ┌─────────────────────┐   │
│  │ GET /fruits  │──►│ direct:listFruits│──►│ cxf:bean:fruitSoap- │   │
│  │ POST /fruits │──►│ direct:addFruit  │──►│ Client              │   │
│  │ DELETE /fruits──►│ direct:deleteFr..│──►│ (POJO data format)  │   │
│  └──────┬──────┘   └─────────────────┘   └──────────┬──────────┘   │
│    JSON │                                    SOAP XML│              │
└─────────┼────────────────────────────────────────────┼──────────────┘
          │                                            │
          │ HTTP/JSON                                  │ HTTP/SOAP
          │                                            ▼
┌─────────┴──────────┐                      ┌─────────────────────┐
│  Citrus HTTP Client │                      │  Citrus SOAP Server │
│  (Test initiator)   │                      │  (Test simulation)  │
│                     │                      │                     │
│  Sends JSON requests│                      │  Validates requests  │
│  Validates responses│                      │  Sends responses    │
└────────────────────┘                      └─────────────────────┘
```

**1. REST DSL Facade**: The Camel REST DSL exposes GET/POST/DELETE on `/fruits`. JSON binding mode handles JSON marshalling automatically.

**2. Protocol Bridge**: Each REST endpoint routes to a `direct` endpoint, which sets the WSDL operation name and forwards to the CXF client. The CXF POJO data format converts between Java objects and SOAP XML.

**3. SOAP Fault Handling**: When the SOAP backend returns a fault, the `onException(SoapFault.class)` handler translates it into an HTTP 500 response with the fault message as body.

**4. Citrus Dual-Role Testing**: Citrus acts as both the HTTP client (sending REST requests) and the SOAP server (simulating the backend). This lets the test verify the complete end-to-end flow including JSON-to-SOAP transformation.

## Understanding the Citrus Tests

The example includes two test classes that demonstrate different approaches:

- **`FruitRestSoapBridgeTest`** — validates SOAP request/response using raw XML strings
- **`FruitPojoRestSoapBridgeTest`** — validates SOAP requests using JAXB unmarshalling with typed Java objects

### Test Setup

```java
@QuarkusTest
@CitrusSupport
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitRestSoapBridgeTest implements TestActionSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18080, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

- `@QuarkusTest`: Starts the Quarkus application with the REST and CXF routes
- `@CitrusSupport`: Enables Citrus framework integration
- `@TestInstance(PER_CLASS)`: Shares the server instance across test methods to avoid port conflicts
- `HttpClient`: Citrus HTTP client configured to call the Quarkus REST API on port 8081
- `WebServiceServer`: Citrus SOAP server that simulates the backend SOAP WebService
- `TestActionSupport`: Provides access to Citrus DSL builder methods (`http()`, `soap()`, etc.)

### Test: List All Fruits (XML-based)

```java
@Test
void shouldListFruits() {
    runner.when(
            http().client(fruitRestClient)
                .send()
                .get("/fruits")
                .fork(true)
    );

    runner.then(
            soap().server(soapServer)
                    .receive()
                    .message()
                    .body("<ns:listFruits xmlns:ns=\"...\"/>")
    );

    runner.then(
            soap().server(soapServer)
                    .send()
                    .message()
                    .body("<ns:listFruitsResponse xmlns:ns=\"...\">...</ns:listFruitsResponse>")
    );
}
```

The test uses `fork(true)` on the HTTP client send to execute the REST call in a separate thread (since it blocks until the backend responds). Meanwhile, the Citrus SOAP server receives and validates the incoming SOAP request, then sends back a simulated response.

### Test: Add a Fruit

```java
@Test
void shouldAddFruit() {
    runner.when(
            http().client(fruitRestClient)
                .send()
                .post("/fruits")
                .message()
                .body("""
                    {"name": "Mango", "description": "Tropical fruit"}
                    """)
                .contentType(APPLICATION_JSON_CONTENT_TYPE)
                .fork(true)
    );

    // Server validates the SOAP request contains the Mango fruit
    // Server sends back the updated fruit list
}
```

The Citrus HTTP client sends a JSON payload via POST. The Camel REST DSL deserializes the JSON into a `Fruit` POJO, which the CXF client marshals to SOAP XML. The Citrus SOAP server validates the XML content and sends back a response.

### Test: SOAP Fault Handling

```java
@Test
void shouldHandleSoapFault() {
    runner.when(
            http().client(fruitRestClient)
                .send()
                .delete("/fruits")
                .message()
                .body("""
                    {"name": "Pineapple", "description": "Tropical fruit"}
                    """)
                .contentType(APPLICATION_JSON_CONTENT_TYPE)
                .fork(true)
    );

    runner.then(soap().server(soapServer).receive()...);

    runner.then(
            soap().server(soapServer)
                    .sendFault()
                    .message()
                    .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Server")
                    .faultString("Fruit \"Pineapple\" does not exist.")
    );

    runner.then(
            http().client(fruitRestClient)
                    .receive()
                    .response(500)
                    .message()
                    .validate((message, context) -> {
                        Assertions.assertEquals("Fruit \"Pineapple\" does not exist.", message.getPayload());
                    })
    );
}
```

This test verifies the full error path: the Citrus SOAP server sends a SOAP fault, the Camel route's `onException(SoapFault.class)` handler translates it to an HTTP 500 response, and the Citrus HTTP client validates the error response.

### POJO-Based Test Approach (FruitPojoRestSoapBridgeTest)

The `FruitPojoRestSoapBridgeTest` demonstrates the same scenarios using JAXB marshalling for type-safe server-side validation and `JsonSupport.marshal()` for JSON request body generation.

#### Receiving and Validating with JAXB

```java
runner.then(
        soap().server(soapServer)
                .receive()
                .message()
                .validate(new XmlMarshallingValidationProcessor<JAXBElement<AddFruit>>() {
                    @Override
                    public void validate(JAXBElement<AddFruit> payload, ...) {
                        AddFruit request = payload.getValue();
                        Assertions.assertEquals("Mango", request.getFruit().getName());
                    }
                })
);
```

#### Sending Responses with Marshalled Objects

```java
runner.then(
        soap().server(soapServer)
                .send()
                .message()
                .body(marshal(addFruitResponse(
                        fruit("Apple", "Winter fruit"),
                        fruit("Orange", "Citrus fruit"),
                        mango
                )))
);
```

A `FruitPojoHelper` utility class provides factory methods (`fruit()`, `addFruitResponse()`, `listFruitResponse()`, `deleteFruitResponse()`) to build the JAXB response objects.

#### Test Port Isolation

The POJO test uses a `@TestProfile` to override the `fruit.service.url` property, pointing the CXF client to port 18081 instead of 18080 to avoid port conflicts between test classes:

```java
@TestProfile(FruitPojoRestSoapBridgeTest.TestConfig.class)

public static class TestConfig implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("fruit.service.url", "http://localhost:18081/services/fruits");
    }
}
```

## Key Testing Concepts

### 1. Dual-Role Citrus Testing

This example showcases Citrus acting in two roles simultaneously: as an HTTP client initiating REST requests and as a SOAP server simulating the backend. This validates the full request/response flow through the protocol bridge.

### 2. Forked HTTP Requests

Since the REST call blocks until the SOAP backend responds, the HTTP client send uses `fork(true)` to execute in a separate thread. This lets the test proceed to handle the SOAP server-side interactions:

```java
http().client(fruitRestClient)
    .send()
    .get("/fruits")
    .fork(true)
```

### 3. End-to-End Error Verification

The SOAP fault test verifies the complete error translation chain: HTTP request -> SOAP fault from backend -> HTTP 500 response to client. Both the SOAP server fault and the HTTP error response are validated in the same test.

### 4. TestActionSupport Interface

Both test classes implement `TestActionSupport`, which provides access to Citrus DSL builder methods like `http()`, `soap()`, and `createVariables()` without static imports.

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-rest-to-soap/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**What happens during test execution:**

1. Quarkus starts the application with REST DSL and CXF client routes
2. Citrus SOAP server starts on port 18080 (or 18081 for the POJO test)
3. Citrus HTTP client sends REST requests to the Quarkus application
4. Camel REST DSL deserializes JSON and forwards to CXF client routes
5. CXF client sends SOAP requests to the Citrus SOAP server
6. Citrus server validates requests and returns simulated responses
7. Camel translates SOAP responses back to JSON REST responses
8. Citrus HTTP client validates the REST responses
9. Application shuts down after tests complete

**Expected output:**
```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-rest**: Camel REST DSL for defining REST endpoints
- **camel-quarkus-platform-http**: Platform HTTP transport for serving REST endpoints in Quarkus
- **camel-quarkus-jackson**: JSON marshalling/unmarshalling via Jackson for REST binding mode
- **camel-quarkus-bean**: Apache Camel Bean component for invoking POJO service methods
- **camel-quarkus-cxf-soap**: Apache Camel CXF SOAP component for Quarkus (client and server)
- **quarkus-cxf-codegen**: WSDL2Java code generation for model objects, SEI, and fault classes
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-http**: Adds HTTP client/server support to Citrus
- **citrus-ws**: Adds SOAP WebService client/server support to Citrus
- **citrus-validation-xml**: XML-aware message validation
- **citrus-validation-json**: JSON-aware message validation
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/citrus/reference/html/)
- [Citrus SOAP WebService Module](https://citrusframework.org/docs/endpoints/soap/) - SOAP endpoint reference for Citrus
- [Citrus HTTP Module](https://citrusframework.org/docs/endpoints/http/) - HTTP endpoint reference for Citrus
- [Apache Camel REST DSL](https://camel.apache.org/components/latest/others/rest.html) - Official Camel REST DSL documentation
- [Apache Camel CXF Component](https://camel.apache.org/components/latest/cxf-component.html) - Official Camel CXF documentation
- [Quarkus CXF Extension](https://docs.quarkiverse.io/quarkus-cxf/dev/) - Quarkiverse CXF extension for Quarkus
- [CXF SOAP Client Example](../camel-cxf-soap-client) - Direct SOAP client example without REST facade
- [CXF SOAP Server Example](../camel-cxf-soap) - The counterpart where Camel exposes a SOAP server

## Sample contract

**Purpose**: This example demonstrates how to test a Camel REST-to-SOAP bridge using the Citrus framework.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-http`, `citrus-ws`, `citrus-validation-json`, `citrus-validation-xml`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with HTTP, SOAP, JSON, and XML validation.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-rest-to-soap/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-rest-to-soap /tmp/citrus-camel-rest-to-soap
cd /tmp/citrus-camel-rest-to-soap
mvn clean verify -Dsystem.under.test.mode=embedded
```
