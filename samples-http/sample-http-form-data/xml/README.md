Http form data sample ![Logo][1]
==============

Browser clients are able to post form data as `application/x-www-form-urlencoded` key-value String to the Http server. The HTML forms use `POST` method and build a
form urlencoded String of all form controls and their values as message payload. You can send and receive such form data in Citrus.

Http form data features are also described in detail in [reference guide][4]

Objectives
---------

The test will simulate client and server for form data exchange via Http POST. First of all we need client and server components in the Spring bean configuration:

```xml
<citrus-http:client id="todoClient"
                        request-url="http://localhost:8080"/>
                        
<citrus-http:server id="todoListServer"
              port="8080"
              auto-start="true"
              timeout="10000"/>
```

These components are normal Citrus Http client and server components. In this test we will use both client and server within the same test case in order to demonstrate both sides
of the communication. In a real world scenario you may just have one side client *or* server in your test. 

So lets start writing a client request that uses form urlencoded message content:

```xml
<http:send-request client="todoClient" fork="true">
  <http:POST path="/api/todo">
    <http:headers content-type="application/x-www-form-urlencoded"/>
    <http:body>
      <http:data><![CDATA[title=${todoName}&description=${todoDescription}]]></http:data>
    </http:body>
  </http:POST>
</http:send-request>
```

As you can see we are using a Http `POST` request with form urlencoded message body. The form data uses two fields `title` and `description` with respective values. On the server side we are able 
to receive this form data for validation:

```xml
<http:receive-request server="todoServer">
  <http:POST path="/api/todo">
    <http:headers content-type="application/x-www-form-urlencoded"/>
    <http:body type="plaintext">
      <http:data>{description=[${todoDescription}], title=[${todoName}]}</http:data>
    </http:body>
  </http:POST>
</http:receive-request>

<http:send-response server="todoServer">
  <http:headers status="200" reason-phrase="OK"/>
</http:send-response>
```

The Citrus Http server is automatically handling the form data and converts the data to a list of fields with its values. We can use the plaintext message validation to check that the form data is
as expected. 

In order to use the special `x-www-form-urlencoded` message validation features in Citrus we need to add a new message validator in the configuration.

```xml
<citrus:message-validators>
    <citrus:validator ref="defaultPlaintextMessageValidator"/>
    <citrus:validator class="com.consol.citrus.http.validation.FormUrlEncodedMessageValidator"/>
</citrus:message-validators>
```

The `com.consol.citrus.http.validation.FormUrlEncodedMessageValidator` validator implementation provides convenient form data marshalling that we can use in our test cases when expecting form urlencoded message content.

```xml
<http:receive-request server="todoServer">
  <http:POST path="/api/todo">
    <http:headers content-type="application/x-www-form-urlencoded"/>
    <http:body type="x-www-form-urlencoded">
      <http:payload>
        <form-data xmlns="http://www.citrusframework.org/schema/http/message">
          <content-type>application/x-www-form-urlencoded</content-type>
          <action>/api/todo</action>
          <controls>
            <control name="description">
              <value>${todoDescription}</value>
            </control>
            <control name="title">
              <value>${todoName}</value>
            </control>
          </controls>
        </form-data>
      </http:payload>
    </http:body>
  </http:POST>
</http:receive-request>

<http:send-response server="todoServer">
  <http:headers status="200" reason-phrase="OK"/>
</http:send-response>
```

As you can see we can now use the special message type `x-www-form-urlencoded` which enables the `FormUrlEncodedMessageValidator` mechanism. This allows us to specify the full form data in form of XML
message contents. This is very powerful as we can then use the full Citrus XML message validation power.

This is how to send and receive Http form data in Citrus.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#http-form-urlencoded-data
