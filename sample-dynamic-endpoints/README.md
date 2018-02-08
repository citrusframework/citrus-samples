Dynamic endpoints sample ![Logo][1]
==============

This sample shows how to use dynamic endpoints in Citrus. Read about this feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
Usually we would define Http client endpoint components in Citrus configuration in order to connect to the REST API
server endpoint.

In this sample we use dynamic endpoint uri instead.
    
```java
http()
    .client("http://localhost:8080")
    .send()
    .post("/api/todolist")
    .messageType(MessageType.JSON)
    .contentType("application/json")
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");
```
        
As you can see the send test action defines the Http request uri as endpoint. Citrus will automatically create a Http client
component out of this endpoint uri. Also you can use this approach when receiving the response:

```java
http()
    .client("http://localhost:8080")
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.JSON)
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");
```

The endpoint uri can hold any Citrus endpoint type and is also capable of handling endpoint properties. Let us use that in an
JMS dynamic endpoint.

```java
send("jms:queue:jms.todo.inbound?connectionFactory=activeMqConnectionFactory")
    .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");    
```
        
The JMS endpoint uri defines the queue name and a connection factory as uri parameter. This connection factory is defined 
as Spring bean in the configuration.

```java
@Bean
public ConnectionFactory activeMqConnectionFactory() {
    return new ActiveMQConnectionFactory("tcp://localhost:61616");
}
```
        
This is how to use dynamic endpoint components in Citrus.
                
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

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html#endpoint-components
