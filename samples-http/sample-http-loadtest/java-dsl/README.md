Http load testing sample ![Logo][1]
==============

This sample demonstrates how to setup a simple load test with TestNG parallel testing. Please note that Citrus is not a performance testing tool per say.
But you can quickly setup some load on a system under test. For more sophisticated load testing Citrus may cooperate with tools like JMeter for instance.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
In this sample we want to call the server API in parallel multiple times in order to create a load on the 
system under test.

We need a Http client component in the configuration:

```java
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints.http()
                        .client()
                        .requestUrl("http://localhost:8080")
                        .build();
}
```
        
We want to call the REST API on the todolist server with that client using multiple threads in order to create 
some load on that server. We can do so by adding a TestNG parameter to the annotation of the test.
        
```java
@Test(invocationCount = 250, threadPoolSize = 25)
```
        
TestNG will start *25* threads in parallel that will send **250** requests in total per test to the todolist application. This creates load on that server. When you execute
this test you will see lots of requests and responses exchanged during the test run. At the end you will have 250 test instances per test reporting success or failure.

This creates very basic load testing scenarios. Of course the tests need to be stateless in order to perform in parallel. You may add message selectors on receive
operations in the test and you may have to correlate response messages so the test instances will not steal messages from each other during the test run.

```java
@Test(invocationCount = 250, threadPoolSize = 25)
public class TodoListLoadTestIT extends TestNGCitrusTest {

    @Autowired
    private HttpClient todoClient;

    @Parameters( { "designer" })
    @CitrusTest
    public void testAddTodo(@Optional @CitrusResource TestDesigner designer) {
        designer.http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=citrus:concat('todo_', citrus:randomNumber(10))");

        designer.http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND);
    }

    @Parameters( { "designer" })
    @CitrusTest
    public void testListTodos(@Optional @CitrusResource TestDesigner designer) {
        designer.http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .accept(MediaType.TEXT_HTML_VALUE);

        designer.http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);
    }

}
```
    
There are two test methods one adding a new todo entry with form url encoded Http POST request and one getting the whole list of todo entries with GET request.
Both methods are executed in parallel creating load on the server. The server must respond to all requests with success otherwise the whole test will fail.   

The test uses resource injection with method parameters. This is required for parallel testing. So each test method instance gets a separate test designer instance
to work with.
        
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
 [4]: https://citrusframework.org/reference/html#http
