Message store sample ![Logo][1]
==============

When Citrus exchanges messages with foreign services the messages are stored internally in a message store. This is an in memory
cache that is filled with messages as they are sent and received within the test case. Read about this feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. While exchanging messages with
the todo application Citrus saves all messages to a local message store.

You can access the message store at any time in the test case using message store functions.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .send()
    .post("/api/todolist")
    .name("todoRequest")
    .messageType(MessageType.JSON)
    .contentType(ContentType.APPLICATION_JSON.getMimeType())
    .payload("{\"id\": \"citrus:randomUUID()\", \"title\": \"citrus:concat('todo_', citrus:randomNumber(4))\", \"description\": \"ToDo Description\", \"done\": false}"));

echo("citrus:message(todoRequest)");
```

The send operation above create a new todo entry as Json message payload and sends it to the todo application via Http POST request. The message
receives a name `todoRequest`. This is the name that is used to store the message right before it is sent out. As soon as the message processing is complete the
local store is saving the message for later usage in the test.

You can access the message store using the message store function `citrus:message(name)`. Using the name of the message provides us the message content as it has been sent to the
todo application.

We are also able to apply some JsonPath expression on the stored message:

```java
echo("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.title')");
```

The echo expression above makes access to the local store reading the message named `todoRequest`. The content is then passed to a JsonPath function that is evaluating the todo title with
`$.title` path expression. The result is the title of the todo entry that has been sent before.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.PLAINTEXT)
    .payload("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')"));
```
        
The receive operation has a special message payload which accesses the message store during validation and reads the dynamic todo entry id   that was created in the `todoRequest` message.

This gives us the opportunity to access message content of previously handled messages in Citrus. The local message store is per test instance so messages in the store are only visible to the
current test case instance that has created the message in the store.

Also received messages are automatically saved to the local store. So you can access the message in later test actions very easy.        
                
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
 [4]: https://citrusframework.org/reference/html#local-message-store
