Groovy test sample ![Logo][1]
==============

This sample uses Groovy to write Citrus test cases.

Objectives
---------

Citrus supports multiple languages to write test cases (e.g. XML, Java, Groovy).

This sample uses Groovy code to write a Citrus test case that is run via TestNG.

```java
@Test
@CitrusGroovyTest(packageScan = "com.consol.citrus.samples.todolist")
public void loadGroovyTests() {
}
```
    
This test method is annotated with `@CitrusGroovyTest` annotation and scans a given package for `.groovy` files. 
This tells Citrus to load the Groovy script files in that directory as test cases. Each Groovy file will be run as 
a separate test. This approach is similar to that being used by Cucumber BDD where a single test loads feature files
as tests.

As usually you can use the Spring framework to glue things together with dependency injection. 
The EndpointConfiguration defines a Http client endpoint component that will be used to connect with the todo app system under test.

```java
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints
        .http()
            .client()
            .requestUrl("http://localhost:8080")
        .build();
}
```

In the Groovy test file you can make use of the great domain specific language capabilities provided by Groovy. 
For instance to define test variables you can use a closure like this:
    
```groovy
given:
    variables {
        todoId = "citrus:randomUUID()"
        todoName = "citrus:concat('todo_', citrus:randomNumber(4))"
        todoDescription = "Code some Citrus tests!"
    }
```

The usage of BDD style Given-When-Then labels is optional, but it is a good opportunity to structure the Groovy test.
You can use the full Citrus Java DSL power in the script like sending Http requests.

```groovy
when: //Create entry
    $(http().client(todoClient)
        .send()
        .post("/api/todolist")
        .message()
        .type(MessageType.JSON)
        .contentType("application/json")
        .body("""
            { 
              "id": "${todoId}", 
              "title": "${todoName}",
              "description": "${todoDescription}",
              "done": false
            }
        """))

    $(http().client(todoClient)
        .receive()
        .response(HttpStatus.OK)
        .message()
        .type(MessageType.PLAINTEXT)
        .body("${todoId}"))
```

Of course the test can use multiline Strings and a lot of other syntax sugar provided by Groovy.
As usually the test executed will be reported in the Citrus test results.

```
| ------------------------------------------------------------------------
| 
| CITRUS TEST RESULTS
| 
|  todo-list-it ................................................... SUCCESS
| 
| TOTAL:	1
| SKIPPED:	0 (0.0%)
| FAILED:	0 (0.0%)
| SUCCESS:	1 (100.0%)
| 
| ------------------------------------------------------------------------
```

This is how you can write Groovy scripts to define Citrus test cases.           

Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dsystem.under.test.mode=embedded
    
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
