Data Dictionary sample ![Logo][1]
==============

This sample deals with data dictionaries that translate message content while exchanging data with the todo sample
application. Read about the Citrus data dictionary feature in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. The Json message content is manipulated before
exchanging with the system under test via data dictionaries. The dictionary is added as component to the Spring bean application context.

```java
@Bean
public JsonPathMappingDataDictionary inboundDictionary() {
    JsonPathMappingDataDictionary dataDictionary = new JsonPathMappingDataDictionary();
    dataDictionary.setGlobalScope(false);
    dataDictionary.setMappingFile(new ClassPathResource("dictionary/inbound.properties"));
    return dataDictionary;
}

@Bean
public JsonPathMappingDataDictionary outboundDictionary() {
    JsonPathMappingDataDictionary dataDictionary = new JsonPathMappingDataDictionary();
    dataDictionary.setGlobalScope(false);
    dataDictionary.setMappingFile(new ClassPathResource("dictionary/outbound.properties"));
    return dataDictionary;
}
```
                
We define two dictionaries, one for inbound messages and another for outbound messages. In the dictionary mapping files we can provide several JsonPath
expressions that should be applied to the messages before exchange.

```
$.title=citrus:concat('todo_', citrus:randomNumber(4))
$.description=Description: todo_${todoId}
$.done=false
```

The outbound mappings above generate dynamic test data for message element on the todo Json payloads. The todo title is automatically set to a random string using the `citrus:randomNumber()` function.
Also the _description_ and _done_ field is set to a proper value.

The dictionary can be applied to each send operation in Citrus.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .send()
    .post("/api/todolist")
    .messageType(MessageType.JSON)
    .dictionary("outboundDictionary")
    .contentType(ContentType.APPLICATION_JSON.getMimeType())
    .payload("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}")); 
```
        
As you can see the outbound dictionary overwrites message content before the actual message is sent out. The message payload in the send operation
does not need to set proper values for _title_, _description_ and _done_. These values can be _null_. The dictionary makes sure that the message content is manipulated before
exchanging with the foreign service.

Same mechanism applies for inbound dictionaries. Here we define assertions on message elements that are automatically applied to the receive operation.

```
$.title=todo_${todoId}
$.description=@startsWith('Description: ')@
$.done=false
```
    
The JsonPath expression mappings above make sure that the message validation is manipulated before taking action. This way we are able to set common validation and manipulation steps in
multiple data dictionaries. Multiple send and receive operations can use the dictionary mappings and we are able to manage those mappings on a very central point of
configuration.            
                
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
 [4]: https://citrusframework.org/reference/html#data-dictionaries
