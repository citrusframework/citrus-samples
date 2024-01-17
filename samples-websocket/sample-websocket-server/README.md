Websockets sample ![Logo][1]
==============

This sample shows how to use the Citrus Websocket server to provide a socket on the server so that clients can connect to and send/receive data. 
Citrus Websocket features are also described in detail in [reference guide][4]

Objectives
---------

The sample uses a small Quarkus application that provides a websocket client to connect to the Citrus Websocket server.
All messages sent to the socket get pushed to the connected clients.
Citrus is able to start the socket as a server in order to accept client sessions and broadcast messages to all connected clients.

In the test Citrus will verify client connections and broadcast some data to the clients.

We need a Websocket server component in the configuration:

```java
public static class EndpointConfig {

    private WebSocketEndpoint chatEndpoint;
   
    @BindToRegistry
    public WebSocketEndpoint chatEndpoint() {
        if (chatEndpoint == null) {
            WebSocketServerEndpointConfiguration chatEndpointConfig = new WebSocketServerEndpointConfiguration();
            chatEndpointConfig.setEndpointUri("/chat");
            chatEndpoint = new WebSocketEndpoint(chatEndpointConfig);
        }
      
        return chatEndpoint;
    }
   
    @BindToRegistry
    public WebSocketServer chatServer() {
        return new WebSocketServerBuilder()
                .webSockets(Collections.singletonList(chatEndpoint()))
                .port(8088)
                .autoStart(true)
                .build();
    }
}
```

The server listens on port `8088` and provides a websocket endpoint on `/chat`.
So clients may connect to the socket opening sessions on `http://localhost:8088/chat`.

In the test cases we can receive client sessions with a normal receive action that references the websocket endpoint.

```java
t.then(receive()
        .endpoint(chatEndpoint)
        .message()
        .body("Quarkus wants to join ..."));
```

**NOTE:**  The message `Quarkus wants to join ...` is automatically sent by the sample Quarkus application when the session is opened. 
We can respond with a server side message that is sent to all connected clients.

```java
t.then(send()
        .endpoint(chatEndpoint)
        .message()
        .body("Welcome Quarkus!"));
```

You will see this response printed to the log output of the Quarkus sample application.

The test is able to trigger some client messages on the Quarkus application by sending a Http POST request to the REST API of the Quarkus application.

```java
t.when(http().client("http://localhost:8081")
        .send()
        .post("chat/citrus-user")
        .fork(true)
        .message()
        .body("Hello from Citrus!"));
```

**NOTE:** The test uses a dynamic Http endpoint URL to send the POST request. 
The username is given as a path parameter and the message body represents the message that is sent to the websocket.

Now the test is able to verify the websocket message on the server.
This time the message has been adjusted by the Quarkus client with `>> {username}:` prefix.

```java
t.then(receive()
        .endpoint(chatEndpoint)
        .message()
        .body(">> citrus-user: Hello from Citrus!"));
```

At the very end of the test we can verify the response of the Http POST request.

```java
t.then(http().client("http://localhost:8081")
        .receive()
        .response(HttpStatus.CREATED));
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
