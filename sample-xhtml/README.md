XHTML sample ![Logo][1]
==============

This sample uses XHTML validation features to verify HTML response from a web container. This feature is
also described in detail in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides an index HTML page that displays all todo entries.
We can validate the HTML content in Citrus with XPath expressions. Citrus automatically converts the HTML to
XHTML so XPath expressions can be evaluated accordingly.

The sample tests show how to use this feature. First we define a global namespace for XHTML in
configuration.

    <citrus:namespace-context>
      <citrus:namespace prefix="xh" uri="http://www.w3.org/1999/xhtml"/>
    </citrus:namespace-context>
    
No we can use the XHTML validation feature in the Citrus test.
    
    http()
        .client(todoClient)
        .response(HttpStatus.OK)
        .messageType(MessageType.XHTML)
        .xpath("(//xh:li[@class='list-group-item'])[last()]", "${todoName}");
        
In a Http client response we can set the message type to XHTML. Citrus automatically converts the HTML response to
XHTML so we can use XPath to validation the HTML content.

The XPath expression makes sure the the last todo entry displayed is the todo item that we have added before in the test.
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
    > mvn clean install -Dembedded=true
    
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

    > mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

> mvn integration-test

You can also pick a single test by calling

> mvn integration-test -Ptest=TodoListIT

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
 [4]: http://www.citrusframework.org/reference/html/index.html#validation-xhtml
