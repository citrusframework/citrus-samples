Kafka sample ![Logo][1]
==============

This sample uses Kafka topics in order to place new todo entries in the system under test. The Kafka capabilities are
also described in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a Kafka inbound topic listener for adding new todo entries.
We can send JSON messages in order to create new todo entries that are stored to the in memory storage.

The Citrus project needs a Kafka server that is provided as embedded server cluster. The embedded server is a singleton embedded Zookeeper
server and a single Kafka server with some test topics provided. The Kafka infrastructure is automatically started
with the tests. So the Citrus Kafka producer endpoint just needs to connect to the Kafka server broker.

```xml
<citrus-kafka:embedded-server id="embeddedKafkaServer"
                                  kafka-server-port="9092"
                                  topics="todo.inbound"/>

<citrus-kafka:endpoint id="todoKafkaEndpoint"
                           server="localhost:9092"
                           topic="todo.inbound"/>
```

The endpoint connects to the server cluster and uses the topic `todo.inbound`. We can now place new todo entries to that topic in our test.
    
```xml
<send endpoint="todoKafkaEndpoint">
    <message>
      <data>
        <![CDATA[
          { "title": "${todoName}", "description": "${todoDescription}", "done": ${done} }
        ]]>
      </data>
    </message>
    <header>
      <element name="citrus_kafka_messageKey" value="${todoName}"/>
    </header>
</send>
```
        
We can add a special message header **KafkaMessageHeaders.MESSAGE_KEY** which is the Kafka producer record message key. The message key is automatically serialized/deserialized as String value. 
The Kafka record value is also defined to be a String in JSON format. The todo app will consume the Kafka record and create a proper todo entry from that record.

The Kafka operation is asynchronous so we do not get any response back. Next action in our test deals with validating that the new todo 
entry has been added successfully. Please review the next steps in the sample test that perform proper validation.
        
Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle. The embedded option automatically starts a Jetty web
container before the integration test phase. The todo-list system under test is automatically 
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
 [4]: https://citrusframework.org/reference/html#kafka
