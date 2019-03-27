<!---
TODO: mbu
this file is terribly outdated 
-->

Test behavior sample ![Logo][1]
==============

This sample the usage of Citrus test behaviors when sending and receiving messages to the todo sample
application. Read about test behaviors in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases.

This time we want to reuse specific message exchange logic by using test behaviors in our tests. The test behavior is a
class that provides a set of test actions for reuse in other test cases. A typical test behavior looks like this:

```java
public class HelloBehavior extends AbstractTestBehavior {
    private final String name;

    public HelloBehavior(String name) {
        this.name = name;
    }

    @Override
    public void apply() {
        echo("Hello " + name);
    }
}
```
    
The behavior extends `AbstractTestBehavior` and defines its logic in the apply method where you can use all Citrus Java fluent API methods as you would do in a normal test.
The behavior is then applied in your test as follows:

```java
@Test
@CitrusTest
public void testHelloBehavior() {
    applyBehavior(new HelloBehavior("Howard"));
    applyBehavior(new HelloBehavior("Leonard"));
    applyBehavior(new HelloBehavior("Penny"));
}   
```
    
The sample above applies the behavior in the test multiple times with different names. The result will be multiple echo test actions that print out the messages to the console logging.
Now we can use behaviors in order to add new todo entries via Http POST request.             
    
```java
public class AddTodoBehavior extends AbstractTestBehavior {

    private String payloadData;
    private Resource resource;
    
    @Override
    public void apply() {
        HttpClientRequestActionBuilder request = http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType());

        if (StringUtils.hasText(payloadData)) {
            request.payload(payloadData);
        } else if (resource != null) {
            request.payload(resource);
        }

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");
    }

    AddTodoBehavior withPayloadData(String payload) {
        this.payloadData = payload;
        return this;
    }

    AddTodoBehavior withResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
```
    
As you can see the behavior provides support for Json payload inline data as well as file resource payloads. You can use the behavior in test then as follows:

```java
@Test
@CitrusTest
public void testJsonPayloadValidation() {
    variable("todoId", "citrus:randomUUID()");
    variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
    variable("todoDescription", "Description: ${todoName}");
    variable("done", "false");
    
    applyBehavior(new AddTodoBehavior()
                        .withPayloadData("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    
    applyBehavior(new AddTodoBehavior()
                        .withResource(new ClassPathResource("templates/todo.json")));
}
```

The behavior is reused multiple times with different payload input. Now we can add more behaviors for getting todo entries via Http GET requests.

```java
public class GetTodoBehavior extends AbstractTestBehavior {

    private String payloadData;
    private Resource resource;

    private Map<String, Object> validateExpressions = new LinkedHashMap<>();

    @Override
    public void apply() {
        http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType());

        HttpClientResponseActionBuilder response = http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON);

        if (StringUtils.hasText(payloadData)) {
            response.payload(payloadData);
        } else if (resource != null) {
            response.payload(resource);
        }

        validateExpressions.forEach(response::validate);
    }

    GetTodoBehavior validate(String payload) {
        this.payloadData = payload;
        return this;
    }

    GetTodoBehavior validate(Resource resource) {
        this.resource = resource;
        return this;
    }

    GetTodoBehavior validate(String expression, Object expected) {
        validateExpressions.put(expression, expected);
        return this;
    }
}
```
    
This time the behavior provides different approaches how to validate the todo entry that was sent as a Json response payload. We can use payload inline data, file resource and JsonPath expressions:

```java
applyBehavior(new GetTodoBehavior()
                    .validate("$.id", "${todoId}")
                    .validate("$.title", "${todoName}")
                    .validate("$.description", "${todoDescription}")
                    .validate("$.done", false));   
```
                        
This completes the usage of test behaviors in Citrus. This is a great way to centralize common tasks in your project to reusable pieces of Citrus Java DSL code.
                         
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
 [4]: https://citrusframework.org/reference/html#java-dsl-test-behaviors
