JDBC sample ![Logo][1]
==============

This sample uses JDBC database connection to verify stored data in SQL query results sets.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application stores data to a relational database. This sample shows 
the usage of database JDBC validation actions in Citrus. We are able to execute SQL statements on a database target. 
See the [reference guide][4] database chapter for details.

The database source is configured as Spring datasource in the application context ***citrus-context.xml***.
    
    <bean id="todoListDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
      <property name="driverClassName" value="org.hsqldb.jdbcDriver"/>
      <property name="url" value="jdbc:hsqldb:hsql://localhost/testdb"/>
      <property name="username" value="sa"/>
      <property name="password" value=""/>
      <property name="initialSize" value="1"/>
      <property name="maxActive" value="5"/>
      <property name="maxIdle" value="2"/>
    </bean>
    
As you can see we are using a H2 in memory database here.    

Before the test suite is started we create the relational database tables required.

    <citrus:before-suite id="createDatabase">
      <citrus:actions>
        <citrus-test:sql datasource="todoListDataSource">
          <citrus-test:statement>CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255))</citrus-test:statement>
        </citrus-test:sql>
      </citrus:actions>
    </citrus:before-suite>

After the test we delete all test data again.

    <citrus:after-suite id="cleanUpDatabase">
      <citrus:actions>
        <citrus-test:sql datasource="todoListDataSource">
          <citrus-test:statement>DELETE FROM todo_entries</citrus-test:statement>
        </citrus-test:sql>
      </citrus:actions>
    </citrus:after-suite>

In the test case we can reference the datasource in order to access the stored data and
verify the result sets.

    query(todoDataSource)
        .statement("select count(*) as cnt from todo_entries where title = '${todoName}'")
        .validate("cnt", "1");

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
 [4]: http://www.citrusframework.org/reference/html/actions-database.html
