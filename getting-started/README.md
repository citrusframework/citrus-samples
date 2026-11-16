Getting started sample ![Logo][1]
==============

This sample introduces Citrus integration testing across Java, XML, YAML, and Groovy DSLs.

Objectives
---------

Citrus supports multiple languages to write test cases (Java, XML, YAML, and Groovy).

`TodoListIT` runs the same todo-list integration scenario in all four DSLs via TestNG: a Java `@CitrusTest` method plus `@CitrusTestSource` runner methods for XML, YAML, and Groovy.

```java
@Test
@CitrusTest
public void testTodoList() {
    // Java DSL actions...
}

@Test
@CitrusTestSource(type = TestLoader.XML,
        sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.xml")
public void testTodoListXml() {
}

@Test
@CitrusTestSource(type = TestLoader.YAML,
        sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.yaml")
public void testTodoListYaml() {
}

@Test
@CitrusTestSource(type = TestLoader.GROOVY,
        sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.groovy")
public void testTodoListGroovy() {
}
```

`EndpointConfig` defines the Spring HTTP client endpoint bean used across all four DSL variants to connect to the todo application system under test:

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
|  TodoListIT.testTodoList ..................................... SUCCESS
|  todo-list.citrus ............................................ SUCCESS
|  TodoListIT.todo-list.xml .................................... SUCCESS
|  TodoListIT.todo-list.yaml ................................... SUCCESS
| 
| TOTAL:	4
| SKIPPED:	0 (0.0%)
| FAILED:	0 (0.0%)
| SUCCESS:	4 (100.0%)
| 
| ------------------------------------------------------------------------
```

Each variant executes against the same embedded todo-list server instance.

Run
---------

**NOTE:** This test depends on the [todo-app](../apps/todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

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
that you can deploy on any web container. You can find the todo-list sources [here](../apps/todo-app). Up to now we have started an
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

## Java, XML, YAML and Groovy test variants

`TodoListIT` executes the same todo-list scenario in all four DSLs during one Failsafe invocation and one system-under-test lifecycle. XML and YAML sources are in `src/test/resources`; the Groovy source is in `src/test/groovy`. Run all variants with `mvn verify -Dsystem.under.test.mode=embedded`, or select methods with `-Dit.test=TodoListIT#testTodoListXml+testTodoListYaml+testTodoListGroovy`.

To omit an unwanted non-Java variant, delete its `.citrus.xml`, `.citrus.yaml`, or `.citrus.groovy` source and remove the corresponding `@CitrusTestSource` method from `TodoListIT`. Keep the Java `@CitrusTest` method and the other runner methods unchanged.

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#validation-xhtml

## Sample contract

- **Purpose:** Learn a minimal todo-list integration test in Java, XML, YAML, and Groovy.
- **Modules:** `citrus-core`.
- **Runtime:** TestNG (`testng`).
- **DSLs:** Java, XML, YAML, GROOVY.
- **Prerequisites:** JDK 21+ and Maven 3.9+ for a copied Maven sample. Install the shared todo application before running a copy; embedded mode starts and stops it during the build.
- **Citrus version:** 5.1.0, declared in `pom.xml`.

Run from the repository root:

```shell
./mvnw -f getting-started/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Copy this complete sample root outside the repository. No aggregator parent installation is required. For a shared-todo sample, run the first command from the repository root before running the copy:

```shell
./mvnw -f apps/todo-app/pom.xml install -DskipTests
cp -R getting-started /tmp/citrus-getting-started
cd /tmp/citrus-getting-started
mvn clean verify -Dsystem.under.test.mode=embedded
```
