WSDL auto generated sample ![Logo][1]
==============

This sample uses a WSDL (Web Service Description Language) specification to generate tests for the todo app system under test. You can read more about the 
Citrus test generation features in [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application manages todo entries. The application provides a SOAP Web Service API 
for adding new entries and listing all entries. This API is specified using a WSDL file.

The sample API specification looks like this.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<wsdl:definitions xmlns="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tns="http://citrusframework.org/samples/todolist" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" name="TodoList" targetNamespace="http://citrusframework.org/samples/todolist">
  <wsdl:types>
    <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="http://citrusframework.org/samples/todolist"
               targetNamespace="http://citrusframework.org/samples/todolist" elementFormDefault="qualified">

      <xs:element name="addTodoEntryRequest">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="title" type="xs:string"/>
            <xs:element name="description" type="xs:string" minOccurs="0"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>

      <xs:element name="addTodoEntryResponse">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="success" type="xs:boolean"/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>

      <xs:element name="getTodoListRequest"/>

      <xs:element name="getTodoListResponse">
        <xs:complexType>
          <xs:sequence>
            <xs:element name="list">
              <xs:complexType>
                <xs:sequence>
                  <xs:element name="todoEntry" minOccurs="0" maxOccurs="unbounded">
                    <xs:complexType>
                      <xs:sequence>
                        <xs:element name="id" type="xs:string"/>
                        <xs:element name="title" type="xs:string"/>
                        <xs:element name="description" type="xs:string" minOccurs="0"/>
                        <xs:element name="attachment" minOccurs="0">
                          <xs:complexType>
                            <xs:sequence>
                              <xs:element name="cid" type="xs:string"/>
                              <xs:element name="contentType" type="xs:string"/>
                              <xs:element name="data" type="xs:string"/>
                            </xs:sequence>
                          </xs:complexType>
                        </xs:element>
                        <xs:element name="done" type="xs:boolean" minOccurs="0"/>
                      </xs:sequence>
                    </xs:complexType>
                  </xs:element>
                </xs:sequence>
              </xs:complexType>
            </xs:element>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:schema>
  </wsdl:types>

  <wsdl:message name="addTodoEntryRequest">
    <wsdl:part element="tns:addTodoEntryRequest" name="parameters"/>
  </wsdl:message>
  <wsdl:message name="addTodoEntryResponse">
    <wsdl:part element="tns:addTodoEntryResponse" name="parameters"/>
  </wsdl:message>
  <wsdl:message name="getTodoListRequest">
    <wsdl:part element="tns:getTodoListRequest" name="parameters"/>
  </wsdl:message>
  <wsdl:message name="getTodoListResponse">
    <wsdl:part element="tns:getTodoListResponse" name="parameters"/>
  </wsdl:message>

  <wsdl:portType name="TodoList">
    <wsdl:operation name="addTodo">
      <wsdl:input message="tns:addTodoEntryRequest"/>
      <wsdl:output message="tns:addTodoEntryResponse"/>
    </wsdl:operation>
    <wsdl:operation name="listTodos">
      <wsdl:input message="tns:getTodoListRequest"/>
      <wsdl:output message="tns:getTodoListResponse"/>
    </wsdl:operation>
  </wsdl:portType>

  <wsdl:binding name="TodoListSOAP" type="tns:TodoList">
    <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
    <wsdl:operation name="addTodo">
      <soap:operation soapAction="addTodo"/>
      <wsdl:input>
        <soap:body use="literal"/>
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal"/>
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="listTodos">
      <soap:operation soapAction="listTodos"/>
      <wsdl:input>
        <soap:body use="literal"/>
      </wsdl:input>
      <wsdl:output>
        <soap:body use="literal"/>
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>

  <wsdl:service name="TodoList">
    <wsdl:port binding="tns:TodoListSOAP" name="TodoListSOAP">
      <soap:address location="http://localhost:8080/services/ws/todolist"/>
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>
```
   
The WSDL specification defines model objects as well as operations that a client can call.
    
Citrus is able to generate client and server side test cases from that specification. Each operation in the WSDL is the basis for a new test case. The test will use the information given in the
specification to generate a proper send/receive request/response communication with the server using the SOAP operations. 

The test code generation takes place in the Maven build lifecycle and uses the Citrus Maven plugin. You need to add the following plugin configuration to the Maven POM file:
    
```xml
<plugin>
    <groupId>org.citrusframework.mvn</groupId>
    <artifactId>citrus-maven-plugin</artifactId>
    <version>${citrus.version}</version>
    <executions>
      <execution>
        <id>generate-tests</id>
        <phase>generate-test-sources</phase>
        <goals>
          <goal>generate-tests</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <type>java</type>
      <framework>testng</framework>
      <tests>
        <test>
          <endpoint>todoClient</endpoint>
          <disabled>true</disabled>
          <wsdl>
            <file>${project.basedir}/src/test/resources/schema/TodoList.wsdl</file>
          </swagger>
        </test>
      </tests>
    </configuration>
</plugin>
```
        
The plugin test generation binds to the `generate-test-sources` Maven lifecycle phase and generates the tests in a default output directory `target/generated/citrus`. The plugin needs the path to the WSDL specification file
which is usually a `.wsdl` file in your project.
 
When executed the plugin generates the Citrus tests to the target output directory. You can add those generated files to the Maven sources/resources with another plugin called `build-helper-maven-plugin`.
        
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
      <execution>
        <id>add-test-sources</id>
        <phase>generate-test-sources</phase>
        <goals>
          <goal>add-test-source</goal>
        </goals>
        <configuration>
          <sources>
            <source>${project.build.directory}/generated/citrus/java</source>
          </sources>
        </configuration>
      </execution>
      <execution>
        <id>add-test-resources</id>
        <phase>generate-test-resources</phase>
        <goals>
          <goal>add-test-resource</goal>
        </goals>
        <configuration>
          <resources>
            <resource>
              <directory>${project.build.directory}/generated/citrus/resources</directory>
            </resource>
          </resources>
        </configuration>
      </execution>
    </executions>
</plugin>
```
            
The build helper plugin adds the generated tests to the Maven project source/resource compilation. This automatically enables the generated tests for execution with the Maven build lifecycle in test/integration-test phase.

Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dsystem.under.test.mode=embedded
    
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
 [4]: https://citrusframework.org/reference/html#generate
