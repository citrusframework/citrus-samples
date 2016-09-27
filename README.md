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
| [sample-javaconfig](sample-javaconfig)| Uses pure Java POJOs for configuration |
| [sample-jdbc](sample-jdbc)| Validates stored data in relational database |
| [sample-binary](sample-binary)| Shows binary message content handling in Citrus |
| [sample-jms](sample-jms)| Shows JMS queue connectivity |
| [sample-soap](https://github.com/christophd/citrus-samples/sample-soap)| Shows SOAP web service support |
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

* Java 1.7 or higher
Installed JDK 1.7 or higher plus JAVA_HOME environment variable set
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

    > mvn clean install -Dembedded=true
    
The embedded option automatically starts an embedded Jetty Web Server Container before the integration test phase in Maven. After that
the Citrus tests will be able to perform its actions in integration-test phase in Maven. After the tests are finished the embedded Jetty 
container is automatically stopped.

You can also start the Jetty container manually by calling:

    > mvn jetty:run

Execute this command in the respective sample folders and you will get a running Jetty Web Server Container with the system under test deployed.

Once the sample application is deployed and running you can execute the Citrus test cases in that sample folder.
Open a separate command line terminal in that folder and execute the following command.

    > mvn integration-test

This executes all Citrus tests in that sample. You can also pick a single test by calling 

    > mvn integration-test -Ptest=<testname>
    
You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Please read the instructions in each sample folder for different setup options and execution commands.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: https://github.com/christophd/citrus