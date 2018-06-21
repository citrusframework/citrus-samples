Http query param sample ![Logo][1]
==============

Http clients are able to pass information via query parameters in the URL to the server. Citrus is able to set those parameters on client side and validate those
information on server side.

Http features are also described in detail in [reference guide][4]

Objectives
---------

We want to provide a Http GET request with query parameters to a Http server that validates the query parameter values accordingly. First of all we need to have both client and server components
in the Spring bean configuration:

```xml
<citrus-http:client id="todoClient"
                        request-url="http://localhost:8080"/>
                        
<citrus-http:server id="todoListServer"
              port="8080"
              auto-start="true"
              timeout="10000"/>
```

Now we can write a test that acts both as client and server simultaneously. This is just for demonstration purpose. In a real world scenario you would call some system under test
or Citrus would consume requests as a server from a system under test. In this sample we add both client and server interaction in the same test. This is also why we need to fork the Http
request message sending as client.

```xml
<http:send-request client="todoClient" fork="true">
  <http:GET path="/api/todo">
    <http:param name="id" value="citrus:randomUUID()"/>
    <http:param name="title" value="todo_0001"/>
    <http:param name="description" value=""/>
  </http:GET>
</http:send-request>

<http:receive-request server="todoListServer">
  <http:GET path="/api/todo">
    <http:param name="title" value="todo_0001"/>
    <http:param name="description" value="@ignore@"/>
  </http:GET>
</http:receive-request>

<http:send-response server="todoListServer">
  <http:headers status="302"/>
</http:send-response>

<http:receive-response client="todoClient">
  <http:headers status="302" reason-phrase="FOUND"/>
</http:receive-response>
```

As you can see we can add multiple query parameters to the GET request with `http:param` elements. The client send action will automatically add those parameters to the request
URL query string (e.g. http://localhost:8080/api/todo?id=&title=todo_0001&description=). Empty parameter values are supported as well as functions and test variables.

The server receives the GET request and is able to validate the incoming query parameters. The server does not have to validate all parameters. As you can see we just skipped the `id` parameter as
it is a random UUID value that we can not expect. The `title` and `description` parameter is validated with `http:param` elements. In case one of these parameters would be missing or in case the value is not
as expected the test case will fail with validation error. Again validation matchers and `@ignore@` expressions are valid during Http query param validation, too.

This is how to send and receive Http query parameters in Citrus.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#http
