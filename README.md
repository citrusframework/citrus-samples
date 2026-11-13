Citrus samples ![Logo][1]
==============

Executable examples for Citrus integration testing
--------------------------------------------------

This repository contains small, independent Citrus 5.1.0 projects. Each sample focuses on one testing capability, runtime, endpoint, connector, validation technology, or complete showcase. Most samples test the shared [todo application](apps/todo-app/); larger showcases keep their own system under test.

Sample index
------------

- [Getting started](getting-started/) — the smallest Java, XML, YAML, and Groovy example.
- [Application](apps/todo-app/) — the shared todo-list system under test, not a test sample.
- [Core](core/) — endpoint configuration, behaviors, dictionaries, message store, dynamic endpoints, and reporting.
- [Runtimes](runtimes/) — TestNG, JUnit 4, JUnit Jupiter, and Cucumber.
- [Endpoints](endpoints/) — [HTTP](endpoints/http/), [SOAP web services](endpoints/ws/), [JMS](endpoints/jms/), [Kafka](endpoints/kafka/), [mail](endpoints/mail/), [RMI](endpoints/rmi/), [Camel](endpoints/camel/), [FTP/SCP/SFTP](endpoints/ftp/), and [WebSocket](endpoints/websocket/).
- [Connectors](connectors/) — Selenium, Playwright, OpenAPI, and SQL by default; Docker and Kubernetes through the `infrastructure-samples` profile (they need a Docker daemon or Kubernetes cluster).
- [Validation](validation/) — [XML](validation/xml/), [JSON](validation/json/), [Hamcrest](validation/hamcrest/), and [binary messages](validation/binary/).
- [Showcases](showcases/) — bakery, bookstore, flight booking, greeting, incident management, and Quarkus applications.
- [Build tools](build-tools/gradle/) — standalone Gradle build outside the Maven reactor.

Learning path
-------------

1. Run [getting-started](getting-started/) and compare its four equivalent DSL scenarios.
2. Compare [annotation configuration](core/annotation-config/) with [Java configuration](core/java-config/).
3. Choose a test runtime in [runtimes](runtimes/), then explore [HTTP basic](endpoints/http/basic/) or [SOAP basic](endpoints/ws/basic/).
4. Add payload checks with the [XML](validation/xml/) and [JSON](validation/json/) validation samples.
5. Explore transport and browser connectors, then run a complete [showcase](showcases/).

DSL coverage matrix
-------------------

The matrix is maintained by hand. Update it when you add, move, or remove a sample, or change its purpose, main module, runtime, or DSL coverage.

| Sample | Description | Main module | Runtime | Java | XML | YAML | Groovy |
|---|---|---|---|:---:|:---:|:---:|:---:|
| [getting-started](getting-started/) | Runs one scenario in Java, XML, YAML, and Groovy | `citrus-base` | testng | Yes | Yes | Yes | Yes |
| [core/annotation-config](core/annotation-config/) | Configures endpoints with annotations | `citrus-base` | testng | Yes | Yes | Yes | — |
| [core/java-config](core/java-config/) | Configures endpoints with plain Java and Spring configuration classes | `citrus-spring` | testng | Yes | Yes | Yes | — |
| [core/behaviors](core/behaviors/) | Reuses test actions through test behaviors | `citrus-base` | testng | Yes | Yes | Yes | — |
| [core/data-dictionaries](core/data-dictionaries/) | Manipulates messages with data dictionaries | `citrus-base` | testng | Yes | Yes | Yes | — |
| [core/message-store](core/message-store/) | Reads exchanged messages from the internal message store | `citrus-base` | testng | Yes | Yes | Yes | — |
| [core/dynamic-endpoints](core/dynamic-endpoints/) | Creates endpoints dynamically from endpoint URIs | `citrus-base` | testng | Yes | Yes | Yes | — |
| [core/reporting](core/reporting/) | Adds a custom test reporter | `citrus-base` | testng | Yes | Yes | Yes | — |
| [runtimes/testng](runtimes/testng/) | Runs Citrus tests with TestNG | `citrus-testng` | testng | Yes | Yes | Yes | — |
| [runtimes/testng-dataprovider](runtimes/testng-dataprovider/) | Parameterizes tests with TestNG data providers | `citrus-testng` | testng | Yes | Yes | Yes | — |
| [runtimes/junit4](runtimes/junit4/) | Runs Citrus tests with JUnit 4 | `citrus-junit` | junit4 | Yes | Yes | Yes | — |
| [runtimes/junit-jupiter](runtimes/junit-jupiter/) | Runs Citrus tests with JUnit Jupiter | `citrus-junit-jupiter` | junit-jupiter | Yes | Yes | Yes | — |
| [runtimes/cucumber](runtimes/cucumber/) | Writes BDD scenarios with Cucumber | `citrus-cucumber` | cucumber | Yes | — | — | — |
| [runtimes/cucumber-spring](runtimes/cucumber-spring/) | Writes BDD scenarios with Cucumber and Spring injection | `citrus-cucumber` | cucumber | Yes | — | — | — |
| [endpoints/http/basic](endpoints/http/basic/) | Calls a REST API as an HTTP client | `citrus-http` | testng | Yes | Yes | Yes | Yes |
| [endpoints/http/basic-auth](endpoints/http/basic-auth/) | Uses basic authentication on HTTP client and server | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/http/form-data](endpoints/http/form-data/) | Sends and validates `x-www-form-urlencoded` form data | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/http/query-param](endpoints/http/query-param/) | Sends and validates HTTP query parameters | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/http/static-response](endpoints/http/static-response/) | Serves static responses from an HTTP server | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/http/https](endpoints/http/https/) | Uses SSL on HTTP client and server | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/http/load-test](endpoints/http/load-test/) | Calls a REST API from multiple threads for load testing | `citrus-http` | testng | Yes | Yes | Yes | — |
| [endpoints/ws/basic](endpoints/ws/basic/) | Calls a SOAP web service as a client | `citrus-ws` | testng | Yes | Yes | Yes | Yes |
| [endpoints/ws/attachment](endpoints/ws/attachment/) | Sends SOAP attachments to a server | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/mtom](endpoints/ws/mtom/) | Sends and receives MTOM-enabled SOAP attachments | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/ssl](endpoints/ws/ssl/) | Uses SSL on a SOAP web service | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/static-response](endpoints/ws/static-response/) | Serves static responses from a SOAP server | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/ws-addressing](endpoints/ws/ws-addressing/) | Uses WS-Addressing on SOAP client and server | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/ws-security](endpoints/ws/ws-security/) | Uses WS-Security on SOAP client and server | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/ws/wsdl](endpoints/ws/wsdl/) | Generates tests from a WSDL | `citrus-ws` | testng | Yes | — | — | — |
| [endpoints/jms](endpoints/jms/) | Integrates with a JMS message broker | `citrus-jms` | testng | Yes | Yes | Yes | — |
| [endpoints/kafka](endpoints/kafka/) | Integrates with Kafka | `citrus-kafka` | testng | Yes | Yes | Yes | — |
| [endpoints/mail](endpoints/mail/) | Simulates a mail server | `citrus-mail` | testng | Yes | Yes | Yes | — |
| [endpoints/rmi](endpoints/rmi/) | Uses RMI as client and server | `citrus-rmi` | testng | Yes | — | — | — |
| [endpoints/camel](endpoints/camel/) | Interacts with an Apache Camel context and routes | `citrus-camel` | testng | Yes | Yes | Yes | — |
| [endpoints/ftp/ftp](endpoints/ftp/ftp/) | Uses FTP as client and server | `citrus-ftp` | testng | Yes | — | — | — |
| [endpoints/ftp/scp](endpoints/ftp/scp/) | Uses SCP as client and server | `citrus-ftp` | testng | Yes | — | — | — |
| [endpoints/ftp/sftp](endpoints/ftp/sftp/) | Uses SFTP as client and server | `citrus-ftp` | testng | Yes | — | — | — |
| [endpoints/websocket/client](endpoints/websocket/client/) | Connects to a WebSocket as a client | `citrus-websocket` | quarkus | Yes | Yes | Yes | — |
| [endpoints/websocket/server](endpoints/websocket/server/) | Provides a WebSocket server for clients | `citrus-websocket` | quarkus | Yes | Yes | Yes | — |
| [connectors/docker](connectors/docker/) | Runs tests inside Docker infrastructure | `citrus-docker` | testng | Yes | — | — | — |
| [connectors/kubernetes](connectors/kubernetes/) | Runs tests inside Kubernetes infrastructure | `citrus-kubernetes` | testng | Yes | Yes | Yes | — |
| [connectors/selenium](connectors/selenium/) | Tests a web UI with Selenium | `citrus-selenium` | testng | Yes | Yes | Yes | — |
| [connectors/playwright](connectors/playwright/) | Tests a web UI with Playwright | `citrus-playwright` | testng | Yes | Yes | Yes | — |
| [connectors/openapi](connectors/openapi/) | Generates tests from an OpenAPI (Swagger) specification | `citrus-http` | testng | Yes | — | — | — |
| [connectors/sql](connectors/sql/) | Validates data stored in a relational database | `citrus-sql` | testng | Yes | Yes | Yes | — |
| [validation/xml/xml](validation/xml/xml/) | Validates XML with schemas and XPath | `citrus-validation-xml` | testng | Yes | Yes | Yes | — |
| [validation/xml/xhtml](validation/xml/xhtml/) | Validates XHTML content | `citrus-validation-xml` | testng | Yes | Yes | Yes | — |
| [validation/xml/oxm](validation/xml/oxm/) | Marshals XML messages to and from objects | `citrus-validation-xml` | testng | Yes | Yes | Yes | — |
| [validation/json/json](validation/json/json/) | Validates JSON payloads with JsonPath | `citrus-validation-json` | testng | Yes | Yes | Yes | — |
| [validation/json/databind](validation/json/databind/) | Maps JSON messages to and from objects | `citrus-validation-json` | testng | Yes | — | — | — |
| [validation/hamcrest](validation/hamcrest/) | Uses Hamcrest matchers in validation and conditions | `citrus-validation-hamcrest` | testng | Yes | Yes | Yes | — |
| [validation/binary](validation/binary/) | Handles binary message content | `citrus-validation-binary` | testng | Yes | Yes | Yes | — |
| [showcases/bakery](showcases/bakery/) | Bakery order workflow across web, worker, and report services | `citrus-jms`, `citrus-http` | testng | Yes | — | — | — |
| [showcases/bookstore](showcases/bookstore/) | Bookstore SOAP service with JMS backend | `citrus-ws`, `citrus-jms` | testng | Yes | Yes | Yes | — |
| [showcases/flight-booking](showcases/flight-booking/) | Flight booking with JMS, HTTP, and a database | `citrus-jms`, `citrus-http` | testng | Yes | Yes | Yes | — |
| [showcases/greeting](showcases/greeting/) | Greeting service over JMS and message channels | `citrus-jms`, `citrus-spring-integration` | testng | Yes | Yes | Yes | — |
| [showcases/incident](showcases/incident/) | Incident management across SOAP, JMS, and HTTP | `citrus-ws`, `citrus-jms`, `citrus-http` | testng | Yes | Yes | Yes | — |
| [showcases/quarkus](showcases/quarkus/) | Quarkus application with Kafka, HTTP, SQL, and a web UI | `citrus-quarkus` | quarkus | Yes | — | — | — |
| [build-tools/gradle](build-tools/gradle/) | Runs Citrus tests with a Gradle build | `citrus-base` | gradle | Yes | Yes | Yes | — |

Showcase transports
-------------------

Each showcase ships its own system under test and combines several transports:

| Showcase | Kafka | JMS | HTTP | SOAP | Channel | SQL | Mail | Sync | Async |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| [showcases/bakery](showcases/bakery/) | | X | X | | | | X | X | X |
| [showcases/bookstore](showcases/bookstore/) | | X | | X | | | | X | |
| [showcases/flight-booking](showcases/flight-booking/) | | X | X | | | X | | | X |
| [showcases/greeting](showcases/greeting/) | | X | | | X | | | X | X |
| [showcases/incident](showcases/incident/) | | X | X | X | | | | X | X |
| [showcases/quarkus](showcases/quarkus/) | X | | X | | | X | X | X | X |

Prerequisites
-------------

- JDK 21 or newer, with `JAVA_HOME` pointing to the JDK.
- The repository Maven wrapper for in-repository builds, or Maven 3.9+ for copied samples.
- A local Gradle installation for [build-tools/gradle](build-tools/gradle/), which has no Gradle wrapper.
- Additional infrastructure only where a leaf README requires it. Docker and Kubernetes samples are not in the default Maven reactor.

Run samples
-----------

Run every default Maven sample from the repository root:

```shell
./mvnw clean verify -Dsystem.under.test.mode=embedded
```

Run one sample without an aggregator parent:

```shell
./mvnw -f endpoints/http/basic/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Embedded mode starts and stops the sample's system under test with the Maven lifecycle. The standalone Gradle sample uses its own build:

```shell
cd build-tools/gradle
gradle clean build
```

Copy a sample out of the repository
-----------------------------------

Every sample root is self-contained. Aggregator POMs are not parents, so they do not need to be installed. Multi-module showcases must be copied as complete roots.

For samples that use the shared todo application, install that SUT once from the repository before running the copied project:

```shell
./mvnw -f apps/todo-app/pom.xml install -DskipTests
cp -R getting-started /tmp/citrus-getting-started
cd /tmp/citrus-getting-started
mvn clean verify -Dsystem.under.test.mode=embedded
```

Read the leaf README for its exact purpose, runtime, DSLs, prerequisites, and run command. Multi-DSL basic samples also explain how to remove an unwanted non-Java variant and its runner reference.

Information
-----------

For more information see [citrusframework.org][2] and the [Citrus reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
