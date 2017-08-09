Apache Camel sample ![Logo][1]
==============

This sample demonstrates how Citrus is able to interact with Apache Camel. Read more about this in [reference guide][4]

Objectives
---------

Apache Camel project implements the enterprise integration patterns for building mediation and routing rules in your enterprise application. With the Citrus 
Camel support you are able to directly interact with the Apache Camel components and route definitions. You can call Camel routes and receive synchronous response messages. 
You can also simulate the Camel route endpoint with receiving messages and providing simulated response messages.

So we need a Camel route to test.

    <!-- Apache Camel context with route to test -->
    <camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
      <route id="newsRoute">
        <from uri="jms:queue:JMS.Queue.News"/>
        <to uri="log:com.consol.citrus.camel?level=INFO"/>
        <to uri="spring-ws:http://localhost:18009?soapAction=newsFeed"/>
      </route>
    </camelContext>

The Camel route reads from a JMS queue and forwards the message to a SOAP web service endpoint. In a test scenario we need to send messages to the JMS destination and wait for messages on
the SOAP server endpoint. Lets add configuration for this in Citrus:

    <!-- JMS endpoint -->
    <citrus-jms:endpoint id="newsJmsEndpoint"
                       destination-name="JMS.Queue.News"
                       timeout="5000"/>

    <!-- SOAP WebService server-->
    <citrus-ws:server id="newsSoapServer"
                    port="18009"
                    auto-start="true"
                    timeout="10000"/>
       
The components above are used in a Citrus test case.
       
    @Test
    public class NewsFeedIT extends TestNGCitrusTestDesigner {
    
        @CitrusTest(name = "NewsFeed_Ok_IT")
        public void newsFeed_Ok_Test() {
            send("newsJmsEndpoint")
                    .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                                "<nf:Message>Citrus rocks!</nf:Message>" +
                            "</nf:News>");
    
            receive("newsSoapServer")
                    .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                                "<nf:Message>Citrus rocks!</nf:Message>" +
                            "</nf:News>")
                    .header(SoapMessageHeaders.SOAP_ACTION, "newsFeed");
    
            send("newsSoapServer")
                    .header(SoapMessageHeaders.HTTP_STATUS_CODE, "200");
        }
    }
       
As you can see Citrus is both JMS message producer and SOAP server at the same time in a single test. The Apache Camel route in the middle will read the JMS message and forward it to the SOAP
server endpoint where Citrus receives the message for validation purpose. This way we make sure the Camel route is working as expected.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install -Dembedded
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/camel.html
