SOAP attachment sample ![Logo][1]
==============

This sample uses SOAP web services with attachments for adding new todo entries on the todo app system under test. You can read more about the 
Citrus SOAP features in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application manages todo entries. The application provides a SOAP web service
endpoint for adding new entries and listing all entries. In this sample we send SOAP attachments to the server adding additional information
to the todo entries.

The sample tests show how to use this SOAP endpoint as a client. First we define the schema and a global namespace for the SOAP
messages.

    <citrus:schema-repository id="schemaRepository">
      <citrus:schemas>
        <citrus:schema id="todoList" location="classpath:schema/TodoList.xsd"/>
      </citrus:schemas>
    </citrus:schema-repository>
        
    <citrus:namespace-context>
      <citrus:namespace prefix="todo" uri="http://citrusframework.org/samples/todolist"/>
    </citrus:namespace-context>
   
The schema repository holds all known schemas in this project. Citrus will automatically check the syntax rules for incoming messages
then. Next we need a SOAP web service client component:

    <citrus-ws:client id="todoListClient"
                      request-url="http://localhost:8080/services/ws/todolist"/>
                          
    <bean id="messageFactory" class="org.springframework.ws.soap.saaj.SaajSoapMessageFactory"/>
    
The client connects to the web service endpoint on the system under test. In addition to that we define a SOAP message factory that is
responsible for creating the SOAP envelope. 

Now we can use the web service client in the Citrus test.
    
    soap()
        .client(todoClient)
        .send()
        .soapAction("addTodoEntry")
        .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));
        
    soap()
        .client(todoClient)
        .receive()
        .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));
        
The Citrus test sends a request and validates the SOAP response message. The message payload is loaded from external file resources.        
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install -Dembedded
    
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
 [4]: http://www.citrusframework.org/reference/html/soap.html
