Executable test JAR sample ![Logo][1]
==============

This sample shows how to create an executable test jar with all Citrus tests. The test jar is executable via `java -jar` and
executes all packaged integration tests. This is extremely useful when the tests should be used as a deployable artifact for execution on another
server and/or at a later time.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases.

This time we do not use Maven surefire and failsafe plugin for executing the Citrus integration tests immediately. Instead we build an executable 
test jar with all dependencies that is able to execute all tests as a standalone application.
    
First of all we tell Maven to create a test-jar file that hold all test sources in out project. You could do so by adding a special maven-jar-plugin configuration. The jar file contains all test scoped sources and 
uses a classifier `-tests.jar`.

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <version>3.0.2</version>
  <executions>
    <execution>
      <goals>
        <goal>test-jar</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The goal above creates the test-jar for us. You will find it after the build in the Maven output directory `target` as build artifact. The test-jar is not executable and is also not having any dependencies packaged in it.

In order to make the test-jar executable we use a spacial Citrus maven plugin:

```xml
<plugin>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-remote-maven-plugin</artifactId>
  <version>${project.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>test-jar</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

This test-jar plugin will also package all test scoped dependencies to the artifact. In addition to that the plugin will extend the jar file with proper Manifest main class configuration for later execution via `java -jar`.
You should now find a new test-jar file with classifier `-tests-app.jar` in the Maven `target` output folder. This jar is executable via command line with:

```bash
java -jar citrus-sample-test-jar-${project-version}-tests-app.jar
```      

This will execute all Citrus test cases that are packaged within the executable test-jar. For demonstration purpose we have added a `exec-maven-plugin` configuration to the sample that executes the test-jar within your Maven process:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>1.6.0</version>
  <executions>
    <execution>
      <id>run-integration-tests</id>
      <phase>integration-test</phase>
      <goals>
        <goal>exec</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <executable>java</executable>
    <arguments>
      <argument>-jar</argument>
      <argument>${project.build.directory}/citrus-sample-test-jar-${project.version}-tests-app.jar</argument>
      <argument>-package</argument>
      <argument>com.consol.citrus.samples.*</argument>
    </arguments>
  </configuration>
</plugin>
```

The exec plugin is bound to the `integration-test` phase and calls the `java -jar` executable. This will execute all tests in the `-tests-app.jar` package.

Now why do you want to do such kind of test packaging? The Citrus integration tests in the project may interact with a system under test which is deployed on a foreign test server. Due to infrastructure limitations the tests may need to execute on that
very same foreign server instance. So you can create the executable test-jar and deploy that artifact to the foreign server, too. Then test execution and system under test are located on the very same machine which implies are much more simple
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
