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
- [Connectors](connectors/) — Selenium, OpenAPI, and SQL by default; Docker, Kubernetes, and Playwright through the `environment-samples` profile.
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

The matrix is generated from each sample's restricted `sample.yaml`. Update it with `python3 scripts/generate_sample_catalog.py`; CI checks it with `--check`.

<!-- SAMPLE-CATALOG:START -->
| Sample | Module | Runtime | Level | Java | XML | YAML | Groovy |
|---|---|---|---|:---:|:---:|:---:|:---:|
| [core/annotation-config](core/annotation-config/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/java-config](core/java-config/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/behaviors](core/behaviors/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/data-dictionaries](core/data-dictionaries/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/message-store](core/message-store/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/dynamic-endpoints](core/dynamic-endpoints/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [core/reporting](core/reporting/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [runtimes/testng](runtimes/testng/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [runtimes/testng-dataprovider](runtimes/testng-dataprovider/) | `citrus-core` | testng | intermediate | Yes | Yes | Yes | — |
| [runtimes/junit4](runtimes/junit4/) | `citrus-core` | junit4 | intermediate | Yes | Yes | Yes | — |
| [runtimes/junit-jupiter](runtimes/junit-jupiter/) | `citrus-core` | junit-jupiter | intermediate | Yes | Yes | Yes | — |
| [runtimes/cucumber](runtimes/cucumber/) | `citrus-core` | cucumber | intermediate | Yes | — | — | — |
| [runtimes/cucumber-spring](runtimes/cucumber-spring/) | `citrus-core` | cucumber | intermediate | Yes | — | — | — |
| [endpoints/http/basic](endpoints/http/basic/) | `citrus-http` | testng | intermediate | Yes | Yes | Yes | Yes |
| [endpoints/http/basic-auth](endpoints/http/basic-auth/) | `citrus-http` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/http/form-data](endpoints/http/form-data/) | `citrus-http` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/http/query-param](endpoints/http/query-param/) | `citrus-http` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/http/static-response](endpoints/http/static-response/) | `citrus-http` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/http/https](endpoints/http/https/) | `citrus-http` | testng | advanced | Yes | Yes | Yes | — |
| [endpoints/http/load-test](endpoints/http/load-test/) | `citrus-http` | testng | advanced | Yes | Yes | Yes | — |
| [endpoints/ws/basic](endpoints/ws/basic/) | `citrus-ws` | testng | intermediate | Yes | Yes | Yes | Yes |
| [endpoints/ws/attachment](endpoints/ws/attachment/) | `citrus-ws` | testng | intermediate | Yes | — | — | — |
| [endpoints/ws/mtom](endpoints/ws/mtom/) | `citrus-ws` | testng | advanced | Yes | — | — | — |
| [endpoints/ws/ssl](endpoints/ws/ssl/) | `citrus-ws` | testng | advanced | Yes | — | — | — |
| [endpoints/ws/static-response](endpoints/ws/static-response/) | `citrus-ws` | testng | intermediate | Yes | — | — | — |
| [endpoints/ws/ws-addressing](endpoints/ws/ws-addressing/) | `citrus-ws` | testng | advanced | Yes | — | — | — |
| [endpoints/ws/ws-security](endpoints/ws/ws-security/) | `citrus-ws` | testng | advanced | Yes | — | — | — |
| [endpoints/ws/wsdl](endpoints/ws/wsdl/) | `citrus-ws` | testng | intermediate | Yes | — | — | — |
| [endpoints/jms](endpoints/jms/) | `citrus-jms` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/kafka](endpoints/kafka/) | `citrus-kafka` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/mail](endpoints/mail/) | `citrus-mail` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/rmi](endpoints/rmi/) | `citrus-rmi` | testng | intermediate | Yes | — | — | — |
| [endpoints/camel](endpoints/camel/) | `citrus-camel` | testng | intermediate | Yes | Yes | Yes | — |
| [endpoints/ftp/ftp](endpoints/ftp/ftp/) | `citrus-ftp` | testng | intermediate | Yes | — | — | — |
| [endpoints/ftp/scp](endpoints/ftp/scp/) | `citrus-ftp` | testng | intermediate | Yes | — | — | — |
| [endpoints/ftp/sftp](endpoints/ftp/sftp/) | `citrus-ftp` | testng | intermediate | Yes | — | — | — |
| [endpoints/websocket/client](endpoints/websocket/client/) | `citrus-websocket` | quarkus | intermediate | Yes | Yes | Yes | — |
| [endpoints/websocket/server](endpoints/websocket/server/) | `citrus-websocket` | quarkus | intermediate | Yes | Yes | Yes | — |
| [connectors/docker](connectors/docker/) | `citrus-docker` | testng | advanced | Yes | — | — | — |
| [connectors/kubernetes](connectors/kubernetes/) | `citrus-kubernetes` | testng | advanced | Yes | Yes | Yes | — |
| [connectors/selenium](connectors/selenium/) | `citrus-selenium` | testng | intermediate | Yes | Yes | Yes | — |
| [connectors/playwright](connectors/playwright/) | `citrus-playwright` | testng | advanced | Yes | Yes | Yes | — |
| [connectors/openapi](connectors/openapi/) | `citrus-openapi` | testng | intermediate | Yes | — | — | — |
| [connectors/sql](connectors/sql/) | `citrus-sql` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/xml/xml](validation/xml/xml/) | `citrus-validation-xml` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/xml/xhtml](validation/xml/xhtml/) | `citrus-validation-xml` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/xml/oxm](validation/xml/oxm/) | `citrus-validation-xml` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/json/json](validation/json/json/) | `citrus-validation-json` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/json/databind](validation/json/databind/) | `citrus-validation-json` | testng | intermediate | Yes | — | — | — |
| [validation/hamcrest](validation/hamcrest/) | `citrus-validation-hamcrest` | testng | intermediate | Yes | Yes | Yes | — |
| [validation/binary](validation/binary/) | `citrus-validation-binary` | testng | intermediate | Yes | Yes | Yes | — |
| [showcases/bakery](showcases/bakery/) | `showcase-bakery` | testng | showcase | Yes | — | — | — |
| [showcases/bookstore](showcases/bookstore/) | `showcase-bookstore` | testng | showcase | Yes | Yes | Yes | — |
| [showcases/flight-booking](showcases/flight-booking/) | `showcase-flight-booking` | testng | showcase | Yes | Yes | Yes | — |
| [showcases/greeting](showcases/greeting/) | `showcase-greeting` | testng | showcase | Yes | Yes | Yes | — |
| [showcases/incident](showcases/incident/) | `showcase-incident` | testng | showcase | Yes | Yes | Yes | — |
| [showcases/quarkus](showcases/quarkus/) | `showcase-quarkus` | quarkus | showcase | Yes | — | — | — |
| [build-tools/gradle](build-tools/gradle/) | `gradle` | gradle | intermediate | Yes | Yes | Yes | — |
| [getting-started](getting-started/) | `citrus-core` | testng | getting-started | Yes | Yes | Yes | Yes |
<!-- SAMPLE-CATALOG:END -->

Prerequisites
-------------

- JDK 21 or newer, with `JAVA_HOME` pointing to the JDK.
- The repository Maven wrapper for in-repository builds, or Maven 3.9+ for copied samples.
- Additional infrastructure only where a leaf README requires it. Docker, Kubernetes, and Playwright samples are not in the default Maven reactor.

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
