Hamcrest matcher sample ![Logo][1]
==============

This sample shows how to use Hamcrest matcher in validation steps. Read about this feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. 

This time the validation is done using Hamcrest matcher implementations in combination with JsonPath expression evaluation.

```java
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.JSON)
    .validate("$.keySet()", hasItems("id", "title", "description", "done"))
    .validate("$.id", equalTo(todoId))
    .validate("$.title", allOf(startsWith("todo_"), endsWith(todoId)))
    .validate("$.description", anyOf(startsWith("Description:"), nullValue()))
    .validate("$.done", not(true));
```

As you can see we are able to provide Hamcrest matcher instances as expected JsonPath value. The hamcrest matcher is evaluated with the
JsonPath expression result. This way we can construct more complex validations on JsonPath expressions.

Also we can use Hamcrest matcher as condition evaluation when using iterable containers in Citrus:

```java
@Test
@CitrusTest
public void testHamcrestCondition() {
    iterate()
        .condition(lessThanOrEqualTo(5))
        .actions(
            createVariable("todoId", "citrus:randomUUID()"),
            createVariable("todoName", "todo_${i}"),
            createVariable("todoDescription", "Description: ${todoName}"),
            http()
                .client(todoClient)
                .send()
                .post("/api/todolist")
                .messageType(MessageType.JSON)
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"),

            http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("${todoId}")
    );
}
```
   
The iteration condition uses the `lessThanOrEqualTo` Hamcrest matcher in order to evaluate the end of the iteration loop. This time we choose to execute the nested test 
action sequence five times.
                
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
 [4]: https://citrusframework.org/reference/html#validate-with-jsonpath
