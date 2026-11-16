Http REST sample ![Logo][1]
==============

This sample demonstrates the Http REST capabilities in Citrus where Citrus calls REST API on a todo web application. REST features are
also described in detail in [reference guide][4]

Objectives
---------

The [todo-list](../../../apps/todo-app/README.md) sample application provides a REST API for managing todo entries.
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
```
    
In test cases we can reference this client component in order to send REST calls to the server.
    
```java
$(http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    .body("title=${todoName}&description=${todoDescription}"));
```
        
As you can see we are able to send **x-www-form-urlencoded** message content as **POST** request. The response is then validated as **Http 200 OK**.

```java
$(http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK));
```
        
Run
---------

**NOTE:** This test depends on the [todo-app](../../../apps/todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

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
that you can deploy on any web container. You can find the todo-list sources [here](../../../apps/todo-app). Up to now we have started an
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

`TodoListIT` executes the get, post, and lifecycle scenarios in all four DSLs during one Failsafe invocation and one system-under-test lifecycle. XML and YAML sources are in `src/test/resources`; Groovy sources are in `src/test/groovy`. Run all variants with `mvn verify -Dsystem.under.test.mode=embedded`.

To omit an unwanted non-Java variant, delete its `.citrus.xml`, `.citrus.yaml`, or `.citrus.groovy` sources and remove the corresponding `@CitrusTestSource` methods from `TodoListIT`. Keep the Java `@CitrusTest` methods and the other runner methods unchanged.

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#http

## Sample contract

- **Purpose:** Exercise basic HTTP client operations in four equivalent DSLs.
- **Modules:** `citrus-http`.
- **Runtime:** TestNG (`testng`).
- **DSLs:** Java, XML, YAML, GROOVY.
- **Prerequisites:** JDK 21+ and Maven 3.9+ for a copied Maven sample. Install the shared todo application before running a copy; embedded mode starts and stops it during the build.
- **Citrus version:** 5.1.0, declared in `pom.xml`.

Run from the repository root:

```shell
./mvnw -f endpoints/http/basic/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Copy this complete sample root outside the repository. No aggregator parent installation is required. For a shared-todo sample, run the first command from the repository root before running the copy:

```shell
./mvnw -f apps/todo-app/pom.xml install -DskipTests
cp -R endpoints/http/basic /tmp/citrus-endpoints-http-basic
cd /tmp/citrus-endpoints-http-basic
mvn clean verify -Dsystem.under.test.mode=embedded
```
