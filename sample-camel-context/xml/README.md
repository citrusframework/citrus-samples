Apache Camel sample ![Logo][1]
==============

This sample demonstrates how Citrus is able to interact with Apache Camel. Read more about this in [reference guide][4]

Objectives
---------

Apache Camel project implements the enterprise integration patterns for building mediation and routing rules in your enterprise application. With the Citrus 
Camel support you are able to directly interact with the Apache Camel components and route definitions. You can call Camel routes and receive synchronous response messages. 
You can also simulate the Camel route endpoint with receiving messages and providing simulated response messages.

So we need a Camel route to test.

```xml
<!-- Apache Camel context with route to test -->
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="newsRoute">
    <from uri="jms:queue:JMS.Queue.News"/>
    <to uri="log:com.consol.citrus.camel?level=INFO"/>
    <to uri="spring-ws:http://localhost:18009?soapAction=newsFeed"/>
  </route>
</camelContext>
```

The Camel route reads from a JMS queue and forwards the message to a SOAP web service endpoint. In a test scenario we need to send messages to the JMS destination and wait for messages on
the SOAP server endpoint. Lets add configuration for this in Citrus:

```xml
<!-- JMS endpoint -->
<citrus-jms:endpoint id="newsJmsEndpoint"
                   destination-name="JMS.Queue.News"
                   timeout="5000"/>

<!-- SOAP WebService server-->
<citrus-ws:server id="newsSoapServer"
                port="18009"
                auto-start="true"
                timeout="10000"/>
```
       
The components above are used in a Citrus test case.
       
```xml
<testcase name="NewsFeed_Ok_IT">
  <actions>
    <echo>
      <message>Send JMS request message to queue destination</message>
    </echo>

    <http:send-request client="newsJmsEndpoint">
      <http:POST>
        <http:headers>
          <http:header name="Operation" value="HelloService/sayHello"/>
        </http:headers>
        <http:body>
          <http:data>
            <![CDATA[
              <nf:News xmlns:nf="http://citrusframework.org/schemas/samples/news">
                <nf:Message>Citrus rocks!</nf:Message>
              </nf:News>
            ]]>
          </http:data>
        </http:body>
      </http:POST>
    </http:send-request>

    <echo>
      <message>Receive JMS message on queue destination</message>
    </echo>

    <http:receive-request server="newsSoapServer">
      <http:POST>
        <http:headers>
          <http:header name="citrus_soap_action" value="newsFeed"/>
        </http:headers>
        <http:body>
          <http:data>
            <![CDATA[
              <nf:News xmlns:nf="http://citrusframework.org/schemas/samples/news">
                <nf:Message>Citrus rocks!</nf:Message>
              </nf:News>
            ]]>
          </http:data>
        </http:body>
      </http:POST>
    </http:receive-request>

    <http:send-response server="newsSoapServer">
      <http:headers status="200"/>
      <http:body>
        <http:data></http:data>
      </http:body>
    </http:send-response>
  </actions>
</testcase>
```
       
As you can see Citrus is both JMS message producer and SOAP server at the same time in a single test. The Apache Camel route in the middle will read the JMS message and forward it to the SOAP
server endpoint where Citrus receives the message for validation purpose. This way we make sure the Camel route is working as expected.

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

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html#camel
