Citrus samples ![Logo][1]
==============

Sample applications
---------

The Citrus samples applications try to demonstrate how Citrus works in
different integration test scenarios. The projects are executable with Maven
and should give you a detailed picture how Citrus testing works.

Overview
---------

The Citrus samples section contains many small projects that each represents a separate system under test and
some Citrus test cases.

Each sample folder demonstrates a special aspect of how to use Citrus. Most of the samples use a simple todo-list application as
system under test. Please find following list of samples and their primary objective:

| Sample                                | Objective |
|---------------------------------------|:---------:|
| [sample-reporting](sample-reporting)| Shows how to add a custom reporter |
| [sample-docker](sample-docker)| Shows how to use Citrus within Docker infrastructure |
| [sample-kubernetes](sample-kubernetes)| Shows how to use Citrus within Kubernetes infrastructure |
| [sample-gradle](sample-gradle)| Uses Gradle build to execute tests |
| [sample-javaconfig](sample-javaconfig)| Uses pure Java POJOs for configuration |
| [sample-jdbc](sample-jdbc)| Validates stored data in relational database |
| [sample-binary](sample-binary)| Shows binary message content handling in Citrus |
| [sample-json](sample-json)| Shows Json payload validation feature with JsonPath validation |
| [sample-xml](sample-xml)| Shows XML validation feature with schema and Xpath validation |
| [sample-oxm](sample-oxm)| Shows XML object marshalling feature when sending and receiving messages |
| [sample-mail](sample-mail)| Shows mail server activities in Citrus |
| [sample-databind](sample-databind)| Shows JSON object mapping feature when sending and receiving messages |
| [sample-dataprovider](sample-dataprovider)| Shows TestNG data provider usage in Citrus |
| [sample-dynamic-endpoints](sample-dynamic-endpoints)| Shows dynamic endpoint component usage |
| [sample-junit5](sample-junit5)| Shows JUnit5 framework support |
| [sample-jms](sample-jms)| Shows JMS queue connectivity |
| [sample-http](sample-http)| Shows Http REST API calls as a client |
| [sample-http-loadtest](sample-http-loadtest)| Calls REST API on Http server with multiple threads for load testing |
| [sample-http-static-response](sample-http-static-response)| Shows how to setup a static response generating Http server component |
| [sample-http-basic-auth](sample-http-basic-auth)| Shows how to use basic authentication on client and server components |
| [sample-https](sample-https)| Shows how to use SSL connectivity as a client and server |
| [sample-soap](sample-soap)| Shows basic SOAP web service support |
| [sample-soap-attachment](sample-soap-attachment)| Shows how to send SOAP attachments to server |
| [sample-soap-wssecurity](sample-soap-wssecurity)| Shows how to configure SOAP web service client and server with WSSecurity enabled |
| [sample-soap-wsaddressing](sample-soap-wsaddressing)| Shows how to configure SOAP web service client and server with WSAddressing enabled |
| [sample-soap-ssl](sample-soap-ssl)| Shows how to configure SOAP web service with SSL secure connectivity |
| [sample-soap-static-response](sample-soap-static-response)| Shows how to setup a static response generating SOAP web service server component |
| [sample-rmi](sample-rmi)| Shows how to use RMI with Citrus as a client and server |
| [sample-xhtml](sample-xhtml)| Shows XHTML validation feature |
| [sample-camel-context](sample-camel-context)| Interact with Apache Camel context and routes |
| [sample-cucumber](sample-cucumber)| Shows BDD integration with Cucumber |
| [sample-cucumber-spring](sample-cucumber-spring)| Shows BDD integration with Cucumber using Spring Framework injection |
|                                       |           |

Following sample projects cover message transports and technologies. Each of these samples provides a separate system under test applicaiton
that demonstrates the messaging aspect.

| Transport                                    | JMS | Http | SOAP | Channel | Camel | Arquillian | JDBC | SYNC | ASYNC |
|----------------------------------------------|:---:|:----:|:----:|:-------:|:-----:|:----------:|:----:|:----:|:-----:|
| [sample-bakery](sample-bakery)               |  X  |  X   |      |         |       |            |      |  X   |   X   |
| [sample-flightbooking](sample-flightbooking) |  X  |  X   |      |         |       |            |  X   |      |   X   |
| [sample-greeting](sample-greeting)           |  X  |      |      |    X    |       |            |      |  X   |   X   |
| [sample-bookstore](sample-bookstore)         |  X  |      |  X   |         |       |            |      |  X   |       |
| [sample-incident](sample-incident)           |  X  |  X   |  X   |         |       |            |      |  X   |   X   |
| [sample-javaee](sample-javaee)               |  X  |  X   |      |         |       |     X      |      |  X   |   X   |
|                                              |     |      |      |         |       |            |      |      |       |

Pick your sample application for try out and got to the respective folder.

Preconditions
---------

See the preconditions for using the Citrus sample applications:

* Installed JDK 1.8 or higher plus `JAVA_HOME` environment variable set
up and pointing to your Java installation directory

* Apache Maven 3.0.x or higher
The sample projects are executable via Apache Maven (http://maven.apache.org/). You need
ANT installed and running an your machine in order to use this way of executing the
sample applications.

In each of the samples folders you will find the Maven (POM) pom.xml that defines all dependencies and build plugins.

Run
---------

You can run all the samples locally on your machine. We are using the Maven build tool for this.

All samples use some project as system under test. These sample application has to be started before executing any Citrus tests.
Many samples reuse the [todo-list](todo-app) application which is a simple web application that provides a basic REST API. 

You can auto start and deploy the todo-list application within the Maven build by using the following command:

     mvn clean install -Dembedded
    
The embedded option automatically starts an embedded Jetty Web Server Container before the integration test phase in Maven. After that
the Citrus tests will be able to perform its actions in integration-test phase in Maven. After the tests are finished the embedded Jetty 
container is automatically stopped.

You can also start the Jetty container manually by calling:

     mvn jetty:run

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

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://github.com/christophd/citrus
