JUnit sample ![Logo][1]
==============

This sample demonstrates the JUnit support in Citrus. We write some JUnit Citrus test cases that test the REST API of the todo sample application. The JUnit support is
also described in detail in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
Citrus is able to call the API methods as a client in order to validate the Http response messages.

We need a Http client component in the configuration:

```java
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints
        .http()
            .client()
            .requestUrl("http://localhost:8080")
        .build();
}
}
```
    
In test cases we can reference this client component in order to send REST calls to the server. Citrus is able to integrate with JUnit as test execution framework. You can use
the `JUnit4CitrusTestRunner` implementation as base for your test.
    
```java
public class TodoListRunnerIT extends JUnit4CitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    [...]

    @Test
    @CitrusTest
    public void testPost() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(action -> action.client(todoClient)
            .send()
            .post("/todolist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        http(action -> action.client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

}
```
        
The `JUnit4CitrusTestRunner` makes sure that Citrus framework is loaded at startup and all configuration is done properly. Also we need to set the annotation `@CitrusTest` on our test methods in
addition to the normal JUnit `@Test` annotation. This way we can inject Citrus endpoints such as the `todoClient` and we can use the runner Java fluent API in Citrus to send and receive messages using that client component. 

Last not least we can also use resource injection to the test methods using `@CitrusResource` method parameter annotations.

```java
public class TodoListIT extends JUnit4CitrusTest {

    @Autowired
    private HttpClient todoClient;

    [...]

    @Test
    @CitrusTest
    public void testPost(@CitrusResource TestRunner runner) {
        runner.variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        runner.variable("todoDescription", "Description: ${todoName}");

        runner.http(action -> action.client(todoClient)
            .send()
            .post("/todolist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        runner.http(action -> action.client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }
}

}
```
  
We can inject method parameters such as `@CitrusResource` annotated `TestRunner` that is our entrance to the Citrus Java fluent API.

We can use the Citrus Java DSL fluent API in the JUnit test in order to exchange messages with the todo application system under test. The test is a normal JUnit test that is executable via Java IDE or command line using Maven or Gradle.

In order to setup Maven for JUnit we need to add the dependency to the project POM file.

```xml
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>    
```
       
That completes the project setup. We are now ready to execute the tests.
       
Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
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
 [4]: https://citrusframework.org/reference/html#run-with-junit4
