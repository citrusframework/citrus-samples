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

```xml
<citrus:schema-repository id="schemaRepository">
    <citrus:schemas>
        <citrus:schema id="todo" location="classpath:schema/Todo.xsd"/>
    </citrus:schemas>
</citrus:schema-repository>
```

That is all for configuration, now we can use XML as message payload in the test cases.
    
```xml
<http:send-request client="todoClient">
    <http:POST path="/api/todolist">
      <http:headers content-type="application/xml"/>
      <http:body>
        <http:payload>
          <t:todo xmlns:t="http://citrusframework.org/samples/todolist">
            <t:id>${todoId}</t:id>
            <t:title>${todoName}</t:title>
            <t:description>${todoDescription}</t:description>
          </t:todo>
        </http:payload>
      </http:body>
    </http:POST>
</http:send-request>
```
        
As you can see we are able to send the XML data as payload. You can add test variables in message payloads. In a receive 
action we are able to use an expected XML message payload. Citrus performs a XML tree comparison where each element is checked to meet
the expected values.

```xml
<http:receive-response client="todoClient">
    <http:headers status="200" reason-phrase="OK"/>
    <http:body>
      <http:payload>
        <t:todo xmlns:t="http://citrusframework.org/samples/todolist">
          <t:id>${todoId}</t:id>
          <t:title>${todoName}</t:title>
          <t:description>${todoDescription}</t:description>
          <t:done>false</t:done>
        </t:todo>
      </http:payload>
    </http:body>
</http:receive-response>
```

The XMl message payload can be difficult to read when used as String concatenation. Fortunately we can also use file resources as message
payloads.

```xml
<http:receive-response client="todoClient">
    <http:headers status="200" reason-phrase="OK"/>
    <http:body>
      <http:resource file="templates/todo.xml"/>
    </http:body>
</http:receive-response>    
```
        
An alternative approach would be to use Xpath expressions when validating incoming XML messages.

```xml
<http:receive-response client="todoClient">
    <http:headers status="200" reason-phrase="OK"/>
    <http:body>
      <http:validate>
        <http:xpath expression="/t:todo/t:id" value="${todoId}"/>
        <http:xpath expression="/t:todo/t:title" value="${todoName}"/>
        <http:xpath expression="/t:todo/t:description" value="${todoDescription}"/>
        <http:xpath expression="/t:todo/t:done" value="false"/>
      </http:validate>
    </http:body>
</http:receive-response>
```
        
Each expression is evaluated and checked for expected values. XPath is namespace sensitive. So we need to use the correct namespaces
in the expressions. Here we have used a namespace prefix ***t:***. This prefix is defined in a central namespace context in the configuration.
       
```xml
<citrus:namespace-context>
    <citrus:namespace prefix="t" uri="http://citrusframework.org/samples/todolist"/>
</citrus:namespace-context>
```
       
This makes sure that the Xpath expressions are able to find the elements with correct namespaces. Of course you can also specify the 
namespace context for each receive action individually.       
        
```xml
<http:receive-response client="todoClient">
    <http:headers status="200" reason-phrase="OK"/>
    <http:body>
      <http:validate>
        <http:xpath expression="/t:todo/t:id" value="${todoId}"/>
        <http:xpath expression="/t:todo/t:title" value="${todoName}"/>
        <http:xpath expression="/t:todo/t:description" value="${todoDescription}"/>
        <http:xpath expression="/t:todo/t:done" value="false"/>
        <http:namespace prefix="t" value="http://citrusframework.org/samples/todolist"/>
      </http:validate>
    </http:body>
</http:receive-response>
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
