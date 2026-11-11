Citrus samples ![Logo][1]
==============

Sample applications
---------

The Citrus samples applications try to demonstrate how Citrus works in
different integration test scenarios. The projects are executable with Maven
and should give you a detailed picture how Citrus testing works.

This repository uses the Java DSL and configuration in Citrus. In case you are not a Java developer you can also use the plain
XML DSL and configuration notation. The samples using XML can be found in [validation/xml](validation/xml).

Overview
---------

The Citrus samples section contains many small projects that each represents a separate system under test and
some Citrus test cases.

Each sample folder demonstrates a special aspect of how to use Citrus. Most of the samples use a simple todo-list application as
system under test. Please find following list of samples and their primary objective:

| Samples                                                                       |                                      Description                                       |
|-------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------:|
| [sample-reporting](core/reporting)                                          |                           Shows how to add a custom reporter                           |
| [sample-docker](connectors/docker)                                                |                  Shows how to use Citrus within Docker infrastructure                  |
| [sample-kubernetes](connectors/kubernetes)                                        |                Shows how to use Citrus within Kubernetes infrastructure                |
| [build-tools/gradle](build-tools/gradle)                                                |                           Uses Gradle build to execute tests                           |
| [sample-annotation-config](core/annotation-config)                          |                      Uses annotation based endpoint configuration                      |
| [sample-javaconfig](core/java-config)                                        |                         Uses pure Java POJOs for configuration                         |
| [getting-started](getting-started)                                                |                    Uses Java, XML, YAML, and Groovy to define Citrus test cases                     |
| [sample-behaviors](core/behaviors)                                          |                   Shows how to reuse test actions in test behaviors                    |
| [sample-dictionaries](core/data-dictionaries)                                    |         Shows how to incorporate message manipulation using data dictionaries          |
| [sample-message-store](core/message-store)                                  |                       Shows how to access internal message store                       |
| [sample-binary](validation/binary)                                                |                    Shows binary message content handling in Citrus                     |
| [sample-hamcrest](validation/hamcrest)                                            |              Shows Hamcrest matcher support in validation and conditions               |
| [sample-mail](endpoints/mail)                                                    |                         Shows mail server activities in Citrus                         |
| [sample-selenium](connectors/selenium)                                            |                      Perform UI testing with Selenium and Citrus                       |
| [sample-playwright](connectors/playwright)                                          |                     Perform UI testing with Playwright and Citrus |
| [sample-dynamic-endpoints](core/dynamic-endpoints)                          |                         Shows dynamic endpoint component usage                         |
| [sample-jms](endpoints/jms)                                                      |                          Shows JMS message broker integration                          |
| [sample-kafka](endpoints/kafka)                                                  |                                Shows Kafka integration                                 |
| [sample-rmi](endpoints/rmi)                                                      |                Shows how to use RMI with Citrus as a client and server                 |
| [sample-camel-context](endpoints/camel)                                  |                     Interact with Apache Camel context and routes                      |
| Samples DB                                                                    |                                      Description                                       |
| [sample-sql](connectors/sql)                                           |                      Validates stored data in relational database                      |
| Samples JSON                                                                  |                                      Description                                       |
| [sample-json](validation/json/json)                                       |             Shows Json payload validation feature with JsonPath validation             |
| [sample-databind](validation/json/databind)                               |         Shows JSON object mapping feature when sending and receiving messages          |
| Samples XML                                                                   |                                      Description                                       |
| [sample-xml](validation/xml/xml)                                          |             Shows XML validation feature with schema and Xpath validation              |
| [sample-oxm](validation/xml/oxm)                                          |        Shows XML object marshalling feature when sending and receiving messages        |
| [sample-xhtml](validation/xml/xhtml)                                      |                             Shows XHTML validation feature                             |
| Samples FTP/SFTP                                                              |                                      Description                                       |
| [sample-ftp](endpoints/ftp/ftp)                                          |                   Shows FTP client and server interaction in Citrus                    |
| [sample-sftp](endpoints/ftp/sftp)                                        |                   Shows SFTP client and server interaction in Citrus                   |
| [sample-scp](endpoints/ftp/scp)                                          |                   Shows SCP client and server interaction in Citrus                    |
| Samples TestNG                                                                |                                      Description                                       |
| [sample-testng](runtimes/testng)                                 |                             Shows TestNG framework support                             |
| [sample-dataprovider](runtimes/testng-dataprovider)                     |                       Shows TestNG data provider usage in Citrus                       |
| Samples JUnit                                                                 |                                      Description                                       |
| [sample-junit](runtimes/junit4)                                    |                             Shows JUnit4 framework support                             |
| [sample-junit5](runtimes/junit-jupiter)                                  |                             Shows JUnit5 framework support                             |
| Samples Http                                                                  |                                      Description                                       |
| [sample-swagger](connectors/openapi)                                 |                       Auto generate tests from Swagger Open API                        |
| [sample-http](endpoints/http/basic)                                       |                         Shows Http REST API calls as a client                          |
| [sample-http-loadtest](endpoints/http/load-test)                     |          Calls REST API on Http server with multiple threads for load testing          |
| [sample-http-static-response](endpoints/http/static-response)       |         Shows how to setup a static response generating Http server component          |
| [sample-http-query-param](endpoints/http/query-param)                 |            How to use Http form data with `x-www-form-urlencoded` Http POST            |
| [sample-http-form-data](endpoints/http/form-data)                 |                          Exchange form data via Http GET/POST                          |
| [sample-http-basic-auth](endpoints/http/basic-auth)                 |         Shows how to use basic authentication on client and server components          |
| [sample-https](endpoints/http/https)                                     |                Shows how to use SSL connectivity as a client and server                |
| Samples Websockets                                                            |                                      Description                                       |
| [sample-websocket-client](endpoints/websocket/client)          |            Shows how to connect to a Websocket as a client during the test             |
| [sample-websocket-server](endpoints/websocket/server)          |          Shows how to provide a Websocket as a server for clients to connect           |
| Samples SOAP                                                                  |                                      Description                                       |
| [sample-wsdl](endpoints/ws/wsdl)                                       |                             Auto generate tests from WSDL                              |
| [sample-soap](endpoints/ws/basic)                                       |                          Shows basic SOAP web service support                          |
| [sample-soap-mtom](endpoints/ws/mtom)                             |              Shows how to send and receive MTOM enabled SOAP attachments               |
| [sample-soap-attachment](endpoints/ws/attachment)                 |                      Shows how to send SOAP attachments to server                      |
| [sample-soap-wssecurity](endpoints/ws/ws-security)                 |   Shows how to configure SOAP web service client and server with WSSecurity enabled    |
| [sample-soap-wsaddressing](endpoints/ws/ws-addressing)             |  Shows how to configure SOAP web service client and server with WSAddressing enabled   |
| [sample-soap-ssl](endpoints/ws/ssl)                               |          Shows how to configure SOAP web service with SSL secure connectivity          |
| [sample-soap-static-response](endpoints/ws/static-response)       |   Shows how to setup a static response generating SOAP web service server component    |
| Samples Cucumber BDD                                                          |                                      Description                                       |
| [sample-cucumber](runtimes/cucumber)                           |                          Shows BDD integration with Cucumber                           |
| [sample-cucumber-spring](runtimes/cucumber-spring)             |          Shows BDD integration with Cucumber using Spring Framework injection          |
Following sample projects cover message transports and technologies. Each of these samples provides a separate system under test application
that demonstrates the messaging aspect.

| Demo Projects                                     | Kafka | JMS | Http | SOAP | Channel | Camel | SQL | SYNC | ASYNC |
|---------------------------------------------------|:-----:|:---:|:----:|:----:|:-------:|:-----:|:---:|:----:|:-----:|
| [sample-quarkus](showcases/quarkus)             |   X   |     |  X   |      |         |       |  X  |  X   |   X   |
| [sample-bakery](showcases/bakery)               |       |  X  |  X   |      |         |       |     |  X   |   X   |
| [sample-flightbooking](showcases/flight-booking) |       |  X  |  X   |      |         |       |  X  |      |   X   |
| [sample-greeting](showcases/greeting)           |       |  X  |      |      |    X    |       |     |  X   |   X   |
| [sample-bookstore](showcases/bookstore)         |       |  X  |      |  X   |         |       |     |  X   |       |
| [sample-incident](showcases/incident)           |       |  X  |  X   |  X   |         |       |     |  X   |   X   |

Pick your sample application for try out and got to the respective folder.

Preconditions
---------

See the preconditions for using the Citrus sample applications:

* Installed JDK 21 or higher plus `JAVA_HOME` environment variable set
up and pointing to your Java installation directory

* Apache Maven 3.9.x or higher
The sample projects are executable via Apache Maven (http://maven.apache.org/).

In each of the samples folders you will find the Maven (POM) pom.xml that defines all dependencies and build plugins.

Run
---------

You can run all the samples locally on your machine. We are using the Maven build tool for this.

All samples use some project as a system under test. 
These sample application has to be started before executing any Citrus tests.
Many samples reuse the [todo-list](apps/todo-app) application which is a simple web application that provides a basic REST API.

You can auto start and deploy the todo-list application within the Maven build by using the following command:

     mvn clean install -Dsystem.under.test.mode=embedded

The embedded option automatically starts an embedded Jetty Web Server Container before the integration test phase in Maven. After that
the Citrus tests will be able to perform its actions in integration-test phase in Maven. After the tests are finished the embedded Jetty 
container is automatically stopped.

You can also start the Jetty container manually by calling:

     mvn jetty:run-war

Execute this command in the respective sample folders and you will get a running Jetty Web Server Container with the system under test deployed.

Once the sample application is deployed and running you can execute the Citrus test cases in that sample folder.
Open a separate command line terminal in that folder and execute the following command.

     mvn verify

This executes all Citrus tests in that sample. You can also pick a single test by calling 

     mvn verify -Dit.test=<testname>
    
You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Please read the instructions in each sample folder for different setup options and execution commands.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://github.com/citrusframework/citrus
