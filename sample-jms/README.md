JMS sample ![Logo][1]
==============

This sample uses JMS queue destinations in order to place new todo entries in the system under test. The JMS capabilities are
also described in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a JMS inbound message listener for adding new todo entries.
We can send JSON messages in order to create new todo entries that are stored to the in memory storage.

The Citrus project needs a JMS connection factory that is defined in the Spring application context as bean:

    <bean id="connectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
      <property name="brokerURL" value="tcp://localhost:61616" />
    </bean>
    
We use ActiveMQ as message broker so we use the respective connection factory implementation here. The message broker is automatically
started with the Maven build lifecycle.
    
No we can add a new todo entry by sending a JSON message to the JMS queue destination.
    
    send(todoJmsEndpoint)
        .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
        .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");
        
We have to add a special message header **_type** which is required by the system under test for message conversion. The message payload
is the JSON representation of a todo entry model object.

The JMS operation is asynchronous so we do not get any response back. Next action in our test deals with validating that the new todo 
entry has been added successfully. The XPath expression validation makes sure the the last todo entry displayed is the todo item that 
we have added before in the test.

You can read about http and XPath validation features in the sample [xhtml](../sample-xhtml/README.md)
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install -Dembedded=true
    
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

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/index.html#jms
