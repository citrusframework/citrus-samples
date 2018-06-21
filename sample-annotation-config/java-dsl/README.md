Annotation config sample ![Logo][1]
==============

This sample uses on-the-fly Java annotation configuration for endpoints.

Objectives
---------

Usually the Citrus endpoint components are configured in a central Spring application context. However there is another
possibility to configure endpoint components per test case with annotations.

This sample uses Java annotations for adding Citrus endpoint configuration in tests. The test therefor uses a member
variable that is annotated with `@CitrusEndpoint` annotation in combination with `@HttpClientConfig` annotation.
    
This tells Citrus to create a new endpoint for this test class.

```java
@CitrusEndpoint
@HttpClientConfig(requestUrl = "http://localhost:8080")
private HttpClient todoClient;
```
    
In contrast to adding the bean to the Spring application context we define the endpoint using annotation configurations. As usual we are
able to reference this endpoint in any send and receive operation in Citrus Java fluent API.

```java
http()
    .client(todoClient)
    .send()
    .get("/todolist")
    .accept("text/html");
```
        
Citrus automatically injects the endpoint with respective configuration for `requestUrl = http://localhost:8080`. You can use this endpoint
within all test methods in this class.       
        
Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle. The embedded option automatically starts a Jetty web
container before the integration test phase. The todo-list system under test is automatically deployed in this phase.
After that the Citrus test cases are able to interact with the todo-list application in the integration test phase.

During the build you will see Citrus performing some integration tests.
After the tests are finished the embedded Jetty web container and the todo-list application are automatically stopped.

System under test
---------

The sample uses a small todo list application as system under test. The application is a web application
that you can deploy on any web container. You can find the todo-list sources [here](../todo-app). Up to now we have started an 
embedded Jetty web container with automatic deployments during the Maven build lifecycle. This approach is fantastic 
when running automated tests in a continuous build.
  
Unfortunately the Jetty server and the sample application automatically get stopped when the Maven build is finished. 
There may be times we want to test against a standalone todo-list application.  

You can start the sample todo list application in Jetty with this command.

     mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#validation-xhtml
