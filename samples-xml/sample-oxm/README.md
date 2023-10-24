Object marshalling sample ![Logo][1]
==============

This sample demonstrates the usage of object mapping in Citrus. We are able to handle automatic object mapping
when sending and receiving message payloads. Read about this feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API with object mapping in Citrus so that we do not need to write message payload JSON or XML
structures but use the model objects directly in our test cases.

First we need to provide a marshaller component in our Spring configuration:
    
```java
@Bean
public Marshaller marshaller() {
    return new Jaxb2Marshaller("com.consol.citrus.samples.todolist.model");
}
```
    
Citrus provides the Jaxb2 marshaller out-of-the-box. You can just use this in your tests to deal with XML payloads.
Please note that the marshaller supports model object classes in package **com.consol.citrus.samples.todolist.model**.

That is all for configuration, now we can use model objects as message payload in the test cases.
    
```java
@Autowired
private Marshaller marshaller;
    
$(http()
    .client(todoClient)
    .send()
    .post("/api/todolist")
    .contentType(ContentType.APPLICATION_XML.getMimeType())
    .body(new TodoEntry(uuid, "${todoName}", "${todoDescription}"), marshaller));
```
        
As you can see we are able to send the model object as payload. The test variable support is also given. Citrus will automatically marshall the object to a **application/json** message content 
as **POST** request. In a receive action we are able to use a mapping validation callback in order to get access to the model objects of an incoming message payload.

```java
$(http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .validationCallback(new XmlMarshallingValidationCallback<TodoEntry>(marshaller) {
        @Override
        public void validate(TodoEntry todoEntry, Map<String, Object> headers, TestContext context) {
            Assert.assertNotNull(todoEntry);
            Assert.assertEquals(todoEntry.getId(), uuid);
        }
    }));
```
        
The validation callback gets the model object as first method parameter. You can now add some validation logic with assertions on the model object.        

Using Spring Oxm marshallers
---------

In case you want to use a different marshaller implementation for instance those provided by Spring Oxm module you can do so, too.

We need to include the Spring oxm module in the dependencies:

```xml
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-oxm</artifactId>
  <version>${spring.version}</version>
  <scope>test</scope>
</dependency>
```

Then you are able to create a bean with the Spring Oxm marshaller.

```java
@Bean
public Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.citrusframework.samples.todolist.model");
        return new MarshallerAdapter(marshaller);
        }
```

The Citrus Spring module provides a marshaller adapter that you can use to wrap the Spring Oxm marshaller implementation in order to meet the Citrus marshaller type.    

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
 [4]: https://citrusframework.org/reference/html#validation-callback
