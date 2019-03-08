XML sample ![Logo][1]
==============

This sample deals with XML message payloads when sending and receiving messages to the todo sample
application. Read about this feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive XML message structures for validation in our test cases.

As we want to deal with XML data it is a good idea to enable schema validation for incoming messages. Just put your
known schemas to the schema repository and Citrus will automatically validate incoming messages with the available schema rules.

```java
@Bean
public SimpleXsdSchema todoListSchema() {
    return new SimpleXsdSchema(new ClassPathResource("schema/Todo.xsd"));
}

@Bean
public XsdSchemaRepository schemaRepository() {
    XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
    schemaRepository.getSchemas().add(todoListSchema());
    return schemaRepository;
}
```

That is all for configuration, now we can use XML as message payload in the test cases.
    
```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .send()
    .post("/api/todolist")
    .contentType(ContentType.APPLICATION_XML.getMimeType())
    .payload("<todo xmlns=\"http://citrusframework.org/samples/todolist\">" +
                "<id>${todoId}</id>" +
                "<title>${todoName}</title>" +
                "<description>${todoDescription}</description>" +
            "</todo>"));
```
        
As you can see we are able to send the XML data as payload. You can add test variables in message payloads. In a receive 
action we are able to use an expected XML message payload. Citrus performs a XML tree comparison where each element is checked to meet
the expected values.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.PLAINTEXT)
    .payload("${todoId}"));
```

The XMl message payload can be difficult to read when used as String concatenation. Fortunately we can also use file resources as message
payloads.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .payload(new ClassPathResource("templates/todo.xml")));    
```
        
An alternative approach would be to use Xpath expressions when validating incoming XML messages.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .validate("/t:todo/t:id", "${todoId}")
    .validate("/t:todo/t:title", "${todoName}")
    .validate("/t:todo/t:description", "${todoDescription}")
    .validate("/t:todo/t:done", "false"));
```
        
Each expression is evaluated and checked for expected values. XPath is namespace sensitive. So we need to use the correct namespaces
in the expressions. Here we have used a namespace prefix ***t:***. This prefix is defined in a central namespace context in the configuration.
       
```java
@Bean
public NamespaceContextBuilder namespaceContextBuilder() {
    NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
    namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("t", "http://citrusframework.org/samples/todolist"));
    return namespaceContextBuilder;
}
```
       
This makes sure that the Xpath expressions are able to find the elements with correct namespaces. Of course you can also specify the 
namespace context for each receive action individually.       
        
```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .validate("/t:todo/t:id", "${todoId}")
    .validate("/t:todo/t:title", "${todoName}")
    .validate("/t:todo/t:description", "${todoDescription}")
    .validate("/t:todo/t:done", "false"));
```
                
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
 [4]: https://citrusframework.org/reference/html#validation-xml
