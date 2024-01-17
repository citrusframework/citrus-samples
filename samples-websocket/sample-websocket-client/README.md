Websockets sample ![Logo][1]
==============

This sample shows how to use the Citrus Websocket client to connect to a socket on the server and send/receive data. 
Citrus Websocket features are also described in detail in [reference guide][4]

Objectives
---------

The sample uses a small Quarkus application that provides a server side websocket for clients to connect to.
All messages sent to the socket get pushed to the connected clients.
Citrus is able to connect to the socket as a client in order to send/receive all messages via this socket broadcast.

In the test Citrus will connect to the socket and send some data to it.
The same message is received in a next step to verify the message broadcast.

We need a Websocket client component in the configuration:

```java
@BindToRegistry
public WebSocketClient chatClient() {
    return new WebSocketClientBuilder()
    .requestUrl("ws://localhost:8081/chat/citrus")
    .build();
}
```

In the test cases we can reference this client component in order to send REST calls to the server.

```java
t.when(send("http://localhost:8081/chat/citrus-user")
        .message()
        .fork(true)
        .body("Hello from Citrus!"));
```

**NOTE:**  The send action uses `fork=true` option. 
This is because the send operation should not block the test to proceed and verify the server side socket communication.

The Quarkus server socket should accept the connection and process the message sent by the Citrus client.
As a result of this we are able to verify the same message on the client because of the server socket broadcast.
This time the message has been adjusted by the Quarkus server with `>> {username}:` prefix.

```java
t.then(receive()
        .endpoint(chatClient)
        .message()
        .body(">> citrus: Hello Quarkus chat!"));
```

Run
---------

The sample application uses QuarkusTest as a framework to run the tests with JUnit Jupiter. 
So you can compile, package and test the sample with Maven to run the test.
 
```shell
mvn clean verify
```

This executes the complete Maven build lifecycle.
The Citrus test cases are part of the unit testing lifecycle and get executed automatically.
The Quarkus application is also started automatically as part of the test.

During the build you will see Citrus performing some integration tests.

System under test
---------

The sample uses a small Quarkus application that provides the Websocket implementation.
You can read more about Quarkus websocket support in [https://quarkus.io/guides/websockets](https://quarkus.io/guides/websockets).

Up to now we have started the Quarkus sample application as part of the unit test during the Maven build lifecycle. 
This approach is fantastic when running automated tests in a continuous build.
  
There may be times we want to test against a standalone application.  

You can start the sample Quarkus application in DevServices mode with this command.

```shell
mvn quarkus:dev
```

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

```shell
mvn verify
```

You can also pick a single test by calling

```shell
mvn clean verify -Dtest=<testname>
```

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application
and Citrus test client). 
And of course green tests at the very end of the build.

Of course, you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the JUnit Jupiter IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#websocket
