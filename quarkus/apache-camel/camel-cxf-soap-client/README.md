# Apache Camel CXF SOAP Client Integration Testing with Citrus

This example demonstrates how to test a Camel CXF SOAP client using the Citrus framework. The Quarkus Camel application acts as a SOAP client, calling an external FruitService SOAP WebService via a CXF client generated from the WSDL contract. Citrus simulates the external SOAP WebService server, validates incoming SOAP requests, and provides simulated SOAP response data.

This is the counterpart to the [camel-cxf-soap](../camel-cxf-soap) example, which demonstrates the opposite direction (Camel exposes a SOAP server, Citrus acts as a SOAP client).

## What You'll Learn

By the end of this guide, you'll understand:

- How to use the Apache Camel CXF component as a SOAP client in Quarkus
- How to define routes that call an external SOAP WebService via CXF
- How to use WSDL2Java code generation for model objects and the service interface
- How to use Citrus SOAP server to simulate an external WebService
- How to validate incoming SOAP requests on the server side with Citrus
- How to provide simulated SOAP response data with Citrus
- How to test SOAP fault handling from the client perspective
- How to use JAXB marshalling for typed server-side validation
- How to use the Citrus Camel component (`camel().send()`) to trigger Camel routes in tests
- How to verify Camel exchange exceptions with Citrus `camel().receive()`
- How to coordinate asynchronous test actions with Citrus `async()` and `fork(true)`

## The Application Under Test

The Quarkus application uses the Apache Camel CXF SOAP component to call an external FruitService SOAP WebService. Three Camel routes are exposed via `direct` endpoints:

```
direct:listFruits   -> CXF client -> external SOAP listFruits operation
direct:addFruit     -> CXF client -> external SOAP addFruit operation
direct:deleteFruit  -> CXF client -> external SOAP deleteFruit operation
```

### WSDL Contract and WSDL2Java Code Generation

The service contract is defined in `src/main/resources/wsdl/FruitService.wsdl` (the same WSDL used in the server example). The project uses **WSDL2Java code generation** (via `quarkus-cxf-codegen`) to automatically generate all model objects and the service endpoint interface:

```properties
quarkus.cxf.codegen.wsdl2java.includes=wsdl/FruitService.wsdl
```

The generated classes include `Fruit`, `FruitService`, `AddFruit`, `AddFruitResponse`, `DeleteFruit`, `DeleteFruitResponse`, `ListFruits`, `ListFruitsResponse`, and `ObjectFactory`.

### Apache Camel CXF Client Route

The `FruitClientRoutes` class configures a CXF endpoint as a producer (client) and defines routes that call the external SOAP service:

```java
@Produces @ApplicationScoped @Named
CxfEndpoint fruitEndpoint() {
    CxfEndpoint fruitEndpoint = new CxfEndpoint();
    fruitEndpoint.setServiceClass(FruitService.class);
    fruitEndpoint.setAddress(fruitServiceUrl);  // configurable via application.properties
    return fruitEndpoint;
}

from("direct:listFruits")
    .setHeader(CxfConstants.OPERATION_NAME, constant("listFruits"))
    .to("cxf:bean:fruitEndpoint");
```

Key aspects:
- **CXF bean endpoint** as a producer (client) — calls the external SOAP service rather than exposing one
- **POJO data format** (default) automatically marshals Java objects to SOAP XML and unmarshals responses back
- **Operation name header** (`CxfConstants.OPERATION_NAME`) identifies which WSDL operation to invoke
- **Configurable address** — the external service URL is set via `fruit.service.url` property, allowing the test to point it to the Citrus SOAP server

### Understanding the Architecture

```
┌─────────────────────────────────────────────────────┐
│  Quarkus Application                                │
│                                                     │
│  direct:listFruits ──► CxfEndpoint ──► SOAP Request │
│  direct:addFruit   ──► (FruitService  ──► to        │
│  direct:deleteFruit──►  client)       external WS   │
└──────────────────────────────────┬──────────────────┘
                                   │ HTTP/SOAP
                                   ▼
                        ┌─────────────────────┐
                        │  Citrus SOAP Server  │
                        │  (Test simulation)   │
                        │                      │
                        │  Validates requests  │
                        │  Sends responses     │
                        └─────────────────────┘
```

**1. CXF as Producer**: Unlike the server example where CXF receives SOAP requests, here CXF sends SOAP requests to an external service. The `to("cxf:bean:fruitEndpoint")` directive makes the CXF endpoint act as a producer (client).

**2. POJO Data Format**: In POJO mode, the CXF producer marshals Java method parameters to SOAP XML, sends the request, and unmarshals the SOAP response back to Java objects.

**3. Operation Dispatching**: The `CxfConstants.OPERATION_NAME` header tells CXF which WSDL operation to invoke. Each Camel route sets this header before calling the CXF endpoint.

**4. Citrus SOAP Server Simulation**: In tests, a Citrus `WebServiceServer` replaces the real external SOAP service. It receives SOAP requests, validates them, and returns simulated responses.

## Understanding the Citrus Tests

The example includes two test classes that demonstrate different approaches to testing the Camel CXF SOAP client:

- **`FruitSoapClientTest`** — validates SOAP request/response using raw XML strings
- **`FruitPojoSoapClientTest`** — validates SOAP requests using JAXB unmarshalling with typed Java objects

### Test Setup

```java
@QuarkusTest
@CitrusSupport
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitSoapClientTest implements TestActionSupport {

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18080, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

- `@QuarkusTest`: Starts the Quarkus application with the CXF client routes
- `@CitrusSupport`: Enables Citrus framework integration
- `@TestInstance(PER_CLASS)`: Shares the server instance across test methods to avoid port conflicts
- `@CitrusEndpoint` + `@WebServiceServerConfig`: Configures a Citrus SOAP server listening on port 18080
- `TestActionSupport`: Provides access to Citrus test action builders like `soap()`, `camel()`, `async()`, `action()`, and `createVariables()`
- `@BindToRegistry` on `CamelContext`: Registers the Camel context in the Citrus registry so the `camel()` actions can interact with Camel routes
- `@CitrusResource`: Injects the Citrus test action runner

### Test: List All Fruits (XML-based)

```java
@Test
void shouldListFruits() {
    runner.given(createVariables()
            .variable("targetNS", TARGET_NS));

    // Trigger the Camel route asynchronously
    runner.when(
            async().actions(
                    action(context -> {
                        ProducerTemplate template = camelContext.createProducerTemplate();
                        template.requestBody("direct:listFruits", (Object) null);
                    })
            )
    );

    // Citrus server receives and validates the SOAP request
    runner.then(
            soap().server(soapServer)
                    .receive()
                    .message()
                    .body("<ns:listFruits xmlns:ns=\"${targetNS}\"/>")
    );

    // Citrus server sends back the simulated response
    runner.then(
            soap().server(soapServer)
                    .send()
                    .message()
                    .body("<ns:listFruitsResponse xmlns:ns=\"${targetNS}\">...</ns:listFruitsResponse>")
    );
}
```

The test uses `createVariables()` to define the target XML namespace for use in XML body assertions. The `async().actions(...)` triggers the Camel route in a separate thread (since the CXF call blocks until the server responds). The Citrus SOAP server then receives the request, validates the XML body, and sends back a simulated response.

### Test: Add a Fruit

```java
@Test
void shouldAddFruit() {
    Fruit mango = new Fruit();
    mango.setName("Mango");
    mango.setDescription("Tropical fruit");

    runner.when(
            camel().send()
                    .endpoint("direct:addFruit")
                    .fork(true)
                    .message(new DefaultMessage(mango))
    );

    // Server validates the SOAP request contains the Mango fruit
    // Server sends back the updated fruit list
}
```

The test uses the Citrus Camel component (`camel().send()`) to send a `Fruit` POJO to the Camel route. The `fork(true)` option ensures the send happens asynchronously so the test can proceed to handle the server-side interactions. CXF marshals the POJO to SOAP XML, the Citrus server validates the XML content, and sends back a response with the complete fruit list.

### Test: SOAP Fault Handling

```java
@Test
void shouldHandleSoapFault() {
    Fruit pineapple = new Fruit();
    pineapple.setName("Pineapple");
    pineapple.setDescription("Tropical fruit");

    // Trigger delete for a non-existent fruit
    runner.when(
            camel().send()
                    .endpoint("direct:deleteFruit", true)
                    .fork(true)
                    .message(new DefaultMessage(pineapple))
    );

    // Server receives the delete request
    runner.then(soap().server(soapServer).receive()...);

    // Server sends back a SOAP fault
    runner.then(
            soap().server(soapServer)
                    .sendFault()
                    .message()
                    .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Server")
                    .faultString("Fruit \"Pineapple\" does not exist.")
    );

    // Verify the Camel exchange captured the exception
    runner.then(
            camel().receive()
                    .endpoint("direct:deleteFruit", true)
                    .message()
                    .validate((message, context) -> {
                        Assertions.assertNotNull(message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION));
                        Assertions.assertEquals("Fruit \"Pineapple\" does not exist.",
                                message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE));
                    })
    );
}
```

The Citrus server uses `sendFault()` to return a SOAP fault response. The `camel().send()` uses `endpoint("direct:deleteFruit", true)` to enable exception capture on the endpoint. The test then uses `camel().receive()` to verify that the Camel exchange captured the SOAP fault exception, checking both the exception type and the fault message.

### POJO-Based Test Approach (FruitPojoSoapClientTest)

The `FruitPojoSoapClientTest` demonstrates the same scenarios but uses JAXB marshalling for both server-side request validation and response generation.

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
AddFruitResponse response = new AddFruitResponse();
// ... populate response ...

runner.then(
        soap().server(soapServer)
                .send()
                .message()
                .body(marshal(new ObjectFactory().createAddFruitResponse(response)))
);
```

This approach provides type-safe validation using the generated JAXB types, catching mismatches at compile time.

## Key Testing Concepts

### 1. Citrus SOAP Server

Citrus provides a `WebServiceServer` that acts as an embedded SOAP endpoint. It receives incoming SOAP requests and lets you validate them before sending back simulated responses:

```java
@CitrusEndpoint
@WebServiceServerConfig(port = 18080, autoStart = true)
WebServiceServer soapServer;
```

### 2. Citrus Camel Component

The Citrus Camel component provides `camel().send()` and `camel().receive()` actions that integrate directly with Camel routes. This is the preferred way to trigger routes and verify exchange results:

```java
runner.when(
        camel().send()
                .endpoint("direct:addFruit")
                .fork(true)
                .message(new DefaultMessage(fruit))
);
```

The `fork(true)` option runs the Camel exchange asynchronously, which is essential when the route calls a synchronous endpoint (like CXF SOAP) — without forking, the test would block before it could handle the server-side interactions.

### 3. Asynchronous Route Triggering

For tests that don't use the Citrus Camel component, the `async().actions(...)` pattern triggers the route via `ProducerTemplate` in a separate thread:

```java
runner.when(
        async().actions(
                action(context -> {
                    template.requestBody("direct:listFruits", (Object) null);
                })
        )
);
```

### 4. Server-Side Request Validation

The Citrus server validates incoming SOAP requests using XML comparison or JAXB unmarshalling, ensuring the CXF client sends correctly structured SOAP messages.

### 5. Server-Side Response Simulation

The Citrus server sends back simulated SOAP responses, allowing tests to verify the full round-trip without a real external SOAP service.

### 6. SOAP Fault Simulation

The Citrus server can send SOAP faults using `sendFault()`, testing the client's error handling. Combined with `camel().receive()`, you can verify that the Camel exchange correctly captures the fault as an exception.

### 7. Test Profile for Port Isolation

The POJO test uses a `@TestProfile` to override the `fruit.service.url` property, pointing the CXF client to a different port and avoiding port conflicts between test classes:

```java
@TestProfile(FruitPojoSoapClientTest.TestConfig.class)
```

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-cxf-soap-client/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**What happens during test execution:**

1. Quarkus starts the application with CXF client routes
2. Citrus SOAP server starts on port 18080 (or 18081 for the POJO test)
3. Tests trigger Camel routes via `camel().send()` or `ProducerTemplate`
4. Camel CXF client sends SOAP requests to the Citrus server
5. Citrus server validates requests and returns simulated responses
6. Application shuts down after tests complete

**Expected output:**
```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-bean**: Apache Camel Bean component for invoking POJO service methods
- **camel-quarkus-cxf-soap**: Apache Camel CXF SOAP component for Quarkus (client and server)
- **quarkus-cxf-codegen**: WSDL2Java code generation for model objects, SEI, and fault classes
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-camel**: Citrus Camel component for triggering and verifying Camel routes
- **citrus-ws**: Adds SOAP WebService client/server support to Citrus
- **citrus-validation-xml**: XML-aware message validation
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/citrus/reference/html/)
- [Citrus SOAP WebService Module](https://citrusframework.org/docs/endpoints/soap/) - SOAP endpoint reference for Citrus
- [Apache Camel CXF Component](https://camel.apache.org/components/latest/cxf-component.html) - Official Camel CXF documentation
- [Quarkus CXF Extension](https://docs.quarkiverse.io/quarkus-cxf/dev/) - Quarkiverse CXF extension for Quarkus
- [Apache CXF](https://cxf.apache.org/) - Apache CXF project
- [CXF SOAP Server Example](../camel-cxf-soap) - The counterpart example where Citrus acts as a SOAP client

## Sample contract

**Purpose**: This example demonstrates how to test a Camel CXF SOAP client using the Citrus framework.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-camel`, `citrus-ws`, `citrus-validation-xml`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Camel, SOAP, and XML validation.
**Prerequisites**: JDK 21 or newer.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-cxf-soap-client/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-cxf-soap-client /tmp/citrus-camel-cxf-soap-client
cd /tmp/citrus-camel-cxf-soap-client
mvn clean verify -Dsystem.under.test.mode=embedded
```
