Deployable test WAR sample ![Logo][1]
==============

This sample shows how to create an executable test webapp with all Citrus tests. The test war is deployable in any application server and
executes all packaged integration tests at container startup. This is extremely useful when the tests should be used as a deployable artifact for execution on another
server and/or at a later time.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases.

This time we do not use Maven surefire and failsafe plugin for executing the Citrus integration tests immediately. Instead we build an executable 
test webapp WAR with all dependencies that is able to execute all tests as part of a normal web application deployment. First of all we add a dependency to the `citrus-remote-server`
library that is able to execute Citrus tests as part of a web application deployment.
    
```xml
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-remote-server</artifactId>
  <version>${citrus.version}</version>
  <scope>test</scope>
</dependency>
```

After that we tell Maven to also create a WAR web archive during the build. The test-war artifact will hold all test sources and all test scoped dependencies of the current Maven project.

```xml
<plugin>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-remote-maven-plugin</artifactId>
  <version>${project.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>test-war</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

After that you should find a new WAR file with classifier `-citrus-tests.war` in the Maven `target` build output folder. This WAR is deployable to any web application server such as Jetty.

```xml
<plugin>
  <groupId>org.eclipse.jetty</groupId>
  <artifactId>jetty-maven-plugin</artifactId>
  <version>${jetty.version}</version>
  <configuration>
    <war>${settings.localRepository}/com/consol/citrus/samples/citrus-sample-todo/${project.version}/citrus-sample-todo-${project.version}.war</war>
    <httpConnector>
      <port>8080</port>
      <idleTimeout>60000</idleTimeout>
    </httpConnector>
    <contextHandlers>
      <contextHandler implementation="org.eclipse.jetty.maven.plugin.JettyWebAppContext">
        <war>${project.build.directory}/${project.artifactId}-${project.version}-citrus-tests.war</war>
        <contextPath>/tests</contextPath>
        <tempDirectory>${project.build.directory}/tmp/tests</tempDirectory>
      </contextHandler>
    </contextHandlers>
    <stopKey>stopMe</stopKey>
    <stopPort>8088</stopPort>
    <stopWait>10</stopWait>
    <systemProperties>
      <systemProperty>
        <name>file.encoding</name>
        <value>UTF-8</value>
      </systemProperty>
    </systemProperties>
  </configuration>
</plugin>
```      

The jetty-maven-plugin above will deploy both system under test `todo.war` and the Citrus `citrus-tests.war` in a web application container. The test-war is using the context path `/tests`. After the deployment we can trigger the Citrus test execution on that server
with

```bash
http://localhost:8080/tests/run
```
  
This test execution can be bound to the Maven lifecycle via citrus-remote-maven-plugin:

```xml
<plugin>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-remote-maven-plugin</artifactId>
  <version>${citrus.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>test-war</goal>
        <goal>test</goal>
        <goal>verify</goal>
      </goals>
      <configuration>
        <server>
          <url>http://localhost:8080/tests</url>
        </server>
        <run>
          <packages>
            <package>com.consol.citrus.samples.todolist</package>
          </packages>
        </run>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The `test` goal is automatically bound to the `integration-test` lifecycle phase in our Maven build. All deployed Citrus test cases will execute within the WAR deployment. The `verify` goal will automatically bind to the `verify` lifecylce phase and eventually break the build when
tests report failure state. This is useful when tests should not break the build in `integration-test` phase but later on in `verify` phase so cleanup tasks are able to perform in `post-integration-test` phase. This behavior is exactly the same as the default Maven failsafe-plugin for 
integration test execution does provide.  
        
Now why do you want to do such kind of test packaging in a WAR file? The Citrus integration tests in the project may interact with a system under test which is deployed on a foreign test server. Due to infrastructure limitations the tests may need to execute on that
very same foreign server instance. So you can create the executable test-war and deploy that artifact to the foreign server, too. Then test execution and system under test are located on the very same machine which implies a much more simple
configuration and less test infrastructure requirements.
                
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
