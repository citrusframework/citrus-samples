JDBC transaction sample ![Logo][1]
==============

This sample uses a JDBC database connection to verify the transactional behavior of the application.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application stores data to a relational database. This sample shows 
the usage of database transaction validation actions in Citrus.
See the [reference guide][4] database chapter for details.

The database server and its datasource are configured in the endpoint configuration context ***EndpointConfig.java***.
    
```xml
<citrus-jdbc:server id="jdbcServer"
                    port="3306"
                    database-name="testdb"
                    timeout="10000"
                    auto-start="true"
                    auto-transaction-handling="false"/>

<bean id="todoDataSource" class="org.springframework.jdbc.datasource.SingleConnectionDataSource">
  <property name="driverClassName" value="com.consol.citrus.db.driver.JdbcDriver"/>
  <property name="url" value="jdbc:citrus:http://localhost:3306/testdb"/>
  <property name="username" value="sa"/>
  <property name="password" value=""/>
</bean>
```
    
As you can see we are using a citrus database server here which is configured to validate transaction behavior
by setting `auto-transaction-handling="false"`.    

In the test case we can now verify the transactional behavior of our application if a client request hits our API. 

```xml
  <http:send-request client="todoClient" fork="true">
    <http:POST path="/todolist">
      <http:headers content-type="application/x-www-form-urlencoded"/>
      <http:body>
        <http:data>title=${todoName}&amp;description=${todoDescription}</http:data>
      </http:body>
    </http:POST>
  </http:send-request>

  <receive endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation>
          <jdbc:transaction-started/>
        </jdbc:operation>
      </payload>
    </message>
  </receive>

  <send endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation-result>
          <jdbc:success>true</jdbc:success>
        </jdbc:operation-result>
      </payload>
    </message>
  </send>

  <receive endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation>
          <jdbc:execute>
            <jdbc:statement>
              <jdbc:sql>@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@</jdbc:sql>
            </jdbc:statement>
          </jdbc:execute>
        </jdbc:operation>
      </payload>
    </message>
  </receive>

  <send endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation-result affected-rows="1">
          <jdbc:success>true</jdbc:success>
        </jdbc:operation-result>
      </payload>
    </message>
  </send>

  <receive endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation>
          <jdbc:transaction-committed/>
        </jdbc:operation>
      </payload>
    </message>
  </receive>

  <send endpoint="jdbcServer">
    <message>
      <payload>
        <jdbc:operation-result>
          <jdbc:success>true</jdbc:success>
        </jdbc:operation-result>
      </payload>
    </message>
  </send>
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
 [4]: https://citrusframework.org/reference/html#actions-database
