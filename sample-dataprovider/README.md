DataProvider sample ![Logo][1]
==============

This sample demonstrates how to use TestNG data providers in Citrus tests. You can also read about this in [reference guide][4].

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
Citrus is able to call the API methods as a client in order to add new todo entries. In this sample we make use of
the TestNG data provider feature in terms of adding multiple todo entries within on single test.

The data provider is defined in the test case.

    @DataProvider(name = "todoDataProvider")
    public Object[][] todoDataProvider() {
        return new Object[][] {
            new Object[] { "todo1", "Description: todo1", false },
            new Object[] { "todo2", "Description: todo2", true },
            new Object[] { "todo3", "Description: todo3", false }
        };
    }
    
The provider gives us two parameters **todoName** and **todoDescription**. The parameters can be bound to test variables
in the Citrus test with some annotation magic.
    
    @Test(dataProvider = "todoDataProvider")
    @CitrusTest
    @CitrusParameters( { "todoName", "todoDescription", "done" })
    public void testProvider(String todoName, String todoDescription, boolean done) {
        variable("todoId", "citrus:randomUUID()");

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");
        
        [...]    
    }            
        
As you can see we are able to use the name and description values provided by the data provider. When executed the test performs
multiple times with respective values:

    CITRUS TEST RESULTS
    TodoListIT.testPost([todo1, Description: todo1, false]) ............... SUCCESS
    TodoListIT.testPost([todo2, Description: todo2, true]) ............... SUCCESS
    TodoListIT.testPost([todo3, Description: todo3, false]) ............... SUCCESS    
        
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

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html/run-testng-data-providers.html
