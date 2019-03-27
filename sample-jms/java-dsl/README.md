JMS sample ![Logo][1]
==============

This sample uses JMS queue destinations in order to place new todo entries in the system under test. The JMS capabilities are
also described in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a JMS inbound message listener for adding new todo entries.
We can send JSON messages in order to create new todo entries that are stored to the in memory storage.

The Citrus project needs a JMS connection factory that is defined in the Spring application context as bean:

```java
@Bean
public ConnectionFactory connectionFactory() {
    return new ActiveMQConnectionFactory("tcp://localhost:61616");
}
```
    
We use ActiveMQ as message broker so we use the respective connection factory implementation here. The message broker is automatically
started with the Maven build lifecycle.

We can use that connection factory in a JMS endpoint configuration:

```java
@Bean
public JmsEndpoint todoJmsEndpoint() {
    return CitrusEndpoints.
        jms()
            .asynchronous()
            .connectionFactory(connectionFactory())
            .destination("jms.todo.inbound")
        .build();
}
```

The endpoint defines the connection factory and the JMS destination. In our example this is a message queue name `jms.todo.inbound`. JMS topics are also supported read about it in
[reference guide][4].    
    
No we can add a new todo entry by sending a JSON message to the JMS queue destination.
    
```java
send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(todoJmsEndpoint)
    .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
    .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }"));
```
        
We have to add a special message header **_type** which is required by the system under test for message conversion. The message payload
is the JSON representation of a todo entry model object.

The JMS operation is asynchronous so we do not get any response back. Next action in our test deals with validating that the new todo 
entry has been added successfully. The XPath expression validation makes sure the the last todo entry displayed is the todo item that 
we have added before in the test.

You can read about http and XPath validation features in the sample [xhtml](../sample-xhtml/README.md)

In order to demonstrate the receive operation on a JMS queue in Citrus we can trigger a JMS report message on the todo-app server via Http.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .send()
    .get("/todolist")
    .accept(MediaType.TEXT_HTML_VALUE));

http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK));
```

The Http GET request triggers a JMS report generation on the todo-app SUT. The report is sent to a JMS queue destination `jms.todo.report`. We can receive that message within Citrus
with a normal `receive` operation on a JMS endpoint.

```java
receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(todoReportEndpoint)
    .messageType(MessageType.JSON)
    .payload("[{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"attachment\":null, \"done\":true}]")
    .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry"));
```

The action receives the report message from that JMS queue and validates the message content (payload and header).
        
Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle. The embedded option automatically starts a Jetty web
container and a ActiveMQ message broker before the integration test phase. The todo-list system under test is automatically 
deployed in this phase. After that the Citrus test cases are able to interact with the todo-list application in the integration test phase.

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
 
As we want to use the JMS capabilities of the too application we need to start the ActiveMQ message broker first. You can do this with Maven:
 
     mvn activemq:run

This is the first step. You can start the sample todo list application in Jetty with this command.

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
 [4]: https://citrusframework.org/reference/html#jms
