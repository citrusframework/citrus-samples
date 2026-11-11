# Citrus Quarkus Demo ![Logo][1]

This project uses Quarkus to implement a sample event-driven application and shows how to verify the event processing with an automated integration test written in [Citrus](https://citrusframework.org).

## Objectives

The project uses the Quarkus test framework to set up a dev services environment with JUnit Jupiter where the application is running on the local machine.
The Quarkus dev services capabilities will automatically start Testcontainers during the test in order to simulate the surrounding infrastructure
(e.g. PostgreSQL database and the Kafka message broker).

If you want to learn more about Quarkus, please visit its website: [https://quarkus.io/](https://quarkus.io/).

## Quarkus sample application

The Quarkus sample demo application is a food market event-driven application that listens for incoming events of type `booking` and `supply`.

![Food Market App](food-market-app-demo.png)

Users are able to add booking events. Each of them references a product and gives an amount as well as an accepted price in a simple Json object structure.

```json
{ "client": "citrus-test", "product": "Pineapple", "amount":  100, "price":  0.99 }
```

At the same time suppliers may add their individual supply events that again reference a product with an amount and a selling price.

The Quarkus application consumes both event types and as soon as bookings and supplies do match in all criteria the food market application will produce booking-completed and shipping events as a result.

All events are produced and consumed with Kafka event streams.
The domain model objects with their individual status are stored in a PostgreSQL database.

## Adding Citrus to the project

Looking at the Maven `pom.xml` you will see that Citrus is added as a test scoped dependency.
The most convenient way to add Citrus to your project is to import the `citrus-bom`.

```xml
<dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.citrusframework</groupId>
        <artifactId>citrus-bom</artifactId>
        <version>${citrus.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
```

Citrus is very modular. This means you can choose from a wide range of modules that add specific testing capabilities to the project (e.g. citrus-kafka, citrus-http, citrus-mail, ...).
In this sample project we include the following modules as test scoped dependencies:

- citrus-quarkus
- citrus-kafka
- citrus-http
- citrus-sql
- citrus-selenium
- citrus-validation-json
- citrus-validation-text

The `citrus-quarkus` module provides the QuarkusTest resource implementation that enables Citrus on a Quarkus test.

```xml
<dependency>
  <groupId>org.citrusframework</groupId>
  <artifactId>citrus-quarkus</artifactId>
</dependency>
```

The other modules add very specific Citrus capabilities such as validation of a Json message payload.

This completes the dependency setup.
Now we can move on to writing an automated integration test that verifies the Quarkus application.

## Enable Citrus with @QuarkusTest

The test uses an arbitrary `@QuarkusTest` annotation with JUnit Jupiter.
This means that Quarkus takes care of starting the application under test.
It also starts some Testcontainers for the PostgreSQL database and the Kafka message broker.

You can enable the Citrus capabilities on the test by adding the `@CitrusSupport` annotation to the test class.

```java
@QuarkusTest
@CitrusSupport
class FoodMarketApplicationTest {

    @CitrusResource
    TestCaseRunner t;

    @Inject
    ObjectMapper mapper;

    @Test
    void shouldProcessEvents() {
        createBooking();
        
        createSupply();
        
        verifyBookingCompletedEvent();
        
        verifyShippingEvent();
    }
}
```

The Citrus enabled test is able to inject additional resources such as the `TestCaseRunner`.
This runner is the entrance to all Citrus related test actions like send/receive messages or querying and verifying entities in the database.

The test will perform four main actions:
* Create a booking event
* Create a matching supply event
* Verify the booking completed event
* Verify the shipping event

In first version of the test all events will be sent/received via the Kafka message broker.

## Stage #1: Prototyping the test

Citrus is able to send and receive messages via Kafka quite easily.
You can use a dynamic endpoint URL (e.g. `kafka:my-topic-name`) to exchange data.
The message content (header and body) is given with simple inline Json Strings in this first prototype.

```java
@QuarkusTest
@CitrusSupport
public class FoodMarketDemoTest {

    @CitrusResource
    TestCaseRunner t;

    @Test
    void shouldMatchBookingAndSupply() {
        createBooking();

        createSupply();

        verifyBookingCompletedEvent();

        verifyShippingEvent();
    }

    private void createBooking() {
        t.when(send()
                .endpoint("kafka:bookings")
                .message()
                .body("""
                    {
                        "client": "citrus",
                        "product": {
                            "name": "Kiwi"
                        },
                        "amount": 10,
                        "price": 0.99,
                        "shippingAddress": "001, Foo Blvd."
                    }
                """)
        );
    }

    //...
}
```

The injected Citrus `TestCaseRunner` `t` is able to use Gherkin Given-When-Then syntax and references the KafkaEndpoint `kafka:bookings` in the send operation.
The message body is a simple Json String that represents the booking.

The rest of the story is quite easy.
In the same way we can also send a `supply` event and then receive `completed` and `shipping` events in the test.

When receiving the `completed` and `shipping` events the test is able to use the Citrus Json validation power coming with the `citrus-validation-json` module.
Citrus will compare the received Json object with an expected template and make sure that all fields and properties do match as expected.

```java
class FoodMarketApplicationTest {

    // ...

    private void verifyBookingCompletedEvent() {
        t.then(receive()
                .endpoint("kafka:completed?timeout=10000&consumerGroup=citrus-booking")
                .message()
                .body("""
                    {
                        "client": "citrus",
                        "product": "Kiwi",
                        "amount": 10,
                        "status": "COMPLETED"
                    }
                """)
        );
    }
}
```

The Citrus Json validation will now compare the received event with the expected Json object and fail the test when there is a mismatch.

_Citrus Json validation_
```json
{ "client":  "citrus", "product": "Kiwi", "amount": 10, "status": "COMPLETED" }

// compared to

{ "client":  "citrus", "product": "Kiwi", "amount": 10, "status": "COMPLETED" }
```

The Json validation is very powerful.
You can ignore properties (expected value set to `@ignore@`), use validation matchers, functions and test variables.
A mismatch in the order of elements or some difference in the formatting of the Json document is not failing the test.

In case there is a mismatch you will be provided with an error and the test fails accordingly.

## Running the Citrus tests

The Quarkus test framework uses JUnit Jupiter as a test driver.
This means you can run the tests just like any other JUnit test (e.g. from your Java IDE, with Maven).

```shell script
./mvnw test
```

The Citrus test capabilities are added on top of `@QuarkusTest` with the `@CitrusSupport` annotation.
So you will not need any other configuration to empower the tests with Citrus.

## Stage #2: Use endpoint builders and domain model objects

Using the dynamic endpoint `kafka:my-topic-name` may be a good and easy start for prototyping.
When it comes to writing more tests in your project you may want to leverage a central Kafka endpoint configuration and reuse it in multiple tests.

You can add a `@CitrusConfiguration` annotation that loads endpoints from one to many configuration classes.

```java
@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
public class FoodMarketDemoTest {

    @CitrusResource
    TestCaseRunner t;

    @CitrusEndpoint
    KafkaEndpoint supplies;

    @CitrusEndpoint
    KafkaEndpoint bookings;

    @CitrusEndpoint
    KafkaEndpoint completed;

    @CitrusEndpoint
    KafkaEndpoint shipping;

    // code the tests
}
```

In the loaded `CitrusEndpointConfig` class the Kafka endpoint instances get configured for all tests that load the configuration.

```java
public class CitrusEndpointConfig {

    @BindToRegistry
    public KafkaEndpoint bookings() {
        return kafka()
                .asynchronous()
                .topic("bookings")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint supplies() {
        return kafka()
                .asynchronous()
                .topic("supplies")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint shipping() {
        return kafka()
                .asynchronous()
                .topic("shipping")
                .consumerGroup("citrus-shipping")
                .timeout(10000L)
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint completed() {
        return kafka()
                .asynchronous()
                .topic("completed")
                .consumerGroup("citrus-completed")
                .timeout(10000L)
                .build();
    }

    // more endpoints
}
```

The configuration class uses the KafkaEndpoint builder and binds the components to the Citrus registry.
With that configuration you can inject the endpoint instances in your test with the `@CitrusEndpoint` annotation.

Now you can reference the endpoint in Citrus send/receive test actions.

```java
Product product = new Product("Kiwi");
Booking booking = new Booking("citrus", product, 10, 0.99D, TestHelper.createShippingAddress().getFullAddress());

private void createBooking(Booking booking) {
    t.when(send()
        .endpoint(bookings)
        .message()
        .body(marshal(booking))
    );
}
```

Another improvement for the test is to use the domain model object `Booking` as a message payload instead of using the inline Json String.

## Stage #3: Add mail verification

So far the test has been using Kafka endpoints exclusively.
Citrus provides a huge set of components to connect to different messaging transports and technologies.

As a next step the test verifies a mail message that is sent by the Quarkus application when a booking has been completed.

First of all the configuration adds a Citrus mail server.

```java
@BindToRegistry
public MailServer mailServer() {
    return mail().server()
            .port(2222)
            .knownUsers(Collections.singletonList("foodmarket@quarkus.io:foodmarket:secr3t"))
            .autoAccept(true)
            .autoStart(true)
            .build();
}
```

The mail server accepts incoming mail requests on port `2222` and adds some known users.
The Quarkus application then uses the connection credentials in the `application.properties`.

```properties
quarkus.mailer.mock=false
quarkus.mailer.own-host-name=localhost
quarkus.mailer.from=foodmarket@quarkus.io
quarkus.mailer.host=localhost
quarkus.mailer.port=2222

quarkus.mailer.username=foodmarket
quarkus.mailer.password=secr3t
quarkus.mailer.start-tls=OPTIONAL
```

The test is able to reference this mail server to verify the mail sent by Quarkus.

```java
private void verifyBookingCompletedMail(Booking booking) {
    t.then(receive()
            .endpoint(mailServer)
            .message(MailMessage.request()
                    .from("foodmarket@quarkus.io")
                    .to("%s@quarkus.io".formatted(booking.getClient()))
                    .subject("Booking completed!")
                    .body("Hey %s, your booking %s has been completed."
                            .formatted(booking.getClient(), booking.getProduct().getName()), "text/plain")));

    t.then(send()
            .endpoint(mailServer)
            .message(MailMessage.response(250)));
}
```

The mail verification includes the validation of all mail related properties (e.g. from, to, subject, body) and the simulation of the mail server response (250 OK).
This is a good point to simulate a mail server error in order to verify the resilience and the error handling on the Quarkus application.

## Stage #4: Use TestBehaviors

Citrus has the concept of TestBehaviors to reuse a set of test actions in multiple tests.
The mail verification steps may be added into a behavior so many tests can make use of it.

```java
public class VerifyBookingCompletedMail implements TestBehavior {

    private final Booking booking;
    private final MailServer mailServer;

    public VerifyBookingCompletedMail(Booking booking, MailServer mailServer) {
        this.booking = booking;
        this.mailServer = mailServer;
    }

    @Override
    public void apply(TestActionRunner t) {
        t.run(receive()
            .endpoint(mailServer)
            .message(MailMessage.request()
                    .from("foodmarket@quarkus.io")
                    .to("%s@quarkus.io".formatted(booking.getClient()))
                    .subject("Booking completed!")
                    .body("Hey %s, your booking %s has been completed."
                            .formatted(booking.getClient(), booking.getProduct().getName()), "text/plain"))
        );

        t.run(send()
            .endpoint(mailServer)
            .message(MailMessage.response())
        );
    }
}
```

The test is able to apply the behavior quite easily.

```java
private void verifyBookingCompletedMail(Booking booking) {
    t.then(t.applyBehavior(new VerifyBookingCompletedMail(booking, mailServer)));
}
```

## Stage #5: Use the Http REST API

The Quarkus application under test also provides a Http REST API to manage bookings and supplies.

The Citrus test is able to call the REST API with a Http client.

```java
@BindToRegistry
public HttpClient foodMarketApiClient() {
    return http().client()
            .requestUrl("http://localhost:8081")
            .build();
}
```

```java
private void createBooking(Booking booking) {
    t.when(http()
            .client(foodMarketApiClient)
            .send()
            .post("/api/bookings")
            .message()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(marshal(booking))
    );

    t.then(http()
            .client(foodMarketApiClient)
            .receive()
            .response(HttpStatus.CREATED)
            .message()
            .extract(json().expression("$.id", "bookingId"))
    );
}
```

The test now sends a Http POST request to create the booking.
The client is able to verify the Http response `201 CREATED` and also save the generated booking id for later reference in the test.

# Stage #6: Verify entities in the database

The test may also verify the entities saved to the PostgreSQL database.
The `@QuarkusTest` dev services is able to inject the dataSource that connects to the PostgreSQL database Testcontainers that is startes as part of the test.

```java
@Inject
DataSource dataSource;

private void verifyBookingStatus(Booking.Status status) {
    t.then(sql()
        .dataSource(dataSource)
        .query()
        .statement("select status from booking where booking.id=${bookingId}")
        .validate("status", status.name())
    );
}
```

The SQL verification uses the extracted `bookingId` test variable to identify the entity in the database.
The returned result set gets verified with the expected column values (e.g. `status=COMPLETED`)

## Stage #7: Use OpenAPI specification

The Quarkus application also exposes its OpenAPI specification for the REST API.
The Citrus test is able to leverage this specification when sending/receiving Http messages.

```java
private final OpenApiSpecification foodMarketSpec =
            OpenApiSpecification.from("http://localhost:8081/q/openapi");

private void createBooking(Booking booking) {
    t.when(openapi()
        .specification(foodMarketSpec)
        .client(foodMarketApiClient)
        .send("addBooking")
        .message()
        .body(marshal(booking))
    );

    t.then(openapi()
        .specification(foodMarketSpec)
        .client(foodMarketApiClient)
        .receive("addBooking", HttpStatus.CREATED)
        .message()
        .extract(json().expression("$.id", "bookingId"))
    );
}
```

The test loads the OpenAPI specification from the Quarkus application with the URL `http://localhost:8081/q/openapi`.
The specification then is used with the Http send test action.

The action references an operation with the id `addBooking`.

```yaml
---
openapi: 3.0.3
info:
  title: citrus-demo-quarkus API
  version: 1.0.0
servers:
  - url: http://localhost:8080
    description: Auto generated value
  - url: http://0.0.0.0:8080
    description: Auto generated value
paths:
  /api/bookings:
    post:
      operationId: addBooking
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Booking'
      responses:
        "200":
          description: OK
```

The Citrus test action now leverages information given in the specification such as resource path `/api/bookings` and the content type `application/json`.

# Stage #8: UI testing with Selenium

Citrus also integrates with Selenium UI testing.
This means that the test is able to open the browser and navigate to the Quarkus application home URL.
Then the test may simulate user interactions such as clicking on links and buttons on the page.

```java
private void approveBooking() {
    t.given(selenium()
            .browser(browser)
            .start());

    t.given(doFinally().actions(
            selenium()
                    .browser(browser)
                    .stop()));

    t.when(selenium()
            .browser(browser)
            .navigate("http://localhost:8081"));

    t.then(delay().seconds(3));

    t.then(selenium()
            .browser(browser)
            .click()
            .element("id", "${bookingId}"));
}
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding/testing using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


[1]: https://citrusframework.org/img/brand-logo.png "Citrus"
