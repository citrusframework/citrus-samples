Executable test jar sample ![Logo][1]
==============

This sample shows how to create an executable test jar with all Citrus tests. The test jar is executable via `java -jar` and
executes all packaged integration tests. This is extremely useful when the tests should be used as an deployable artifact.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases.

This time we do not use Maven surefire and failsafe plugin for executing the Citrus integration tests. Instead we build a 
test jar with dependencies that is able to execute all tests as standalone application.
    
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

First of all we tell Maven to also create a jar file that hold all test sources. The jar file will have a classifier `-tests.jar`.

Now we add all test scoped dependencies to the jar file.

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-assembly-plugin</artifactId>
  <version>3.1.0</version>
  <configuration>
    <archive>
      <manifest>
        <mainClass>com.consol.citrus.main.CitrusApp</mainClass>
      </manifest>
    </archive>
    <descriptors>
      <descriptor>src/main/assembly/tests-jar.xml</descriptor>
    </descriptors>
  </configuration>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
        <goal>single</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The assembly descriptor builds a fat-jar with all test scoped dependencies.

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
  <id>tests-with-dependencies</id>
  <formats>
    <format>jar</format>
  </formats>
  <includeBaseDirectory>false</includeBaseDirectory>
  <dependencySets>
    <dependencySet>
      <outputDirectory>/</outputDirectory>
      <useProjectArtifact>false</useProjectArtifact>
      <useProjectAttachments>true</useProjectAttachments>
      <unpack>true</unpack>
      <scope>test</scope>
    </dependencySet>
  </dependencySets>
</assembly>
```  

After that you should find a new jar file with classifier `-tests-with-dependencies.jar` in the Maven `target` output folder. This jar is executable via command line with:

```bash
java -jar citrus-sample-test-jar-${project-version}-tests-with-dependencies.jar
```      

This will execute all Citrus test cases that are packaged within the fat-jar. You can also execute the jar within your Maven process:

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
      <argument>${project.build.directory}/citrus-sample-test-jar-${project.version}-tests-with-dependencies.jar</argument>
      <argument>-package</argument>
      <argument>com.consol.citrus.samples.*</argument>
    </arguments>
  </configuration>
</plugin>
```

The exec plugin is bound to the `integration-test` phase and calls the `java -jar` executable. This will execute all tests in the `-tests.jar` package.   
                
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
