Binary sample ![Logo][1]
==============

This sample demonstrates how Citrus handles binary message content. The sample send some binary content to a JMS queue destination and receives
that same content in a next step from the JMS destination.

Objectives
---------

We demonstrate the binary content handling by using binary JMS messages.

The Citrus project needs a JMS connection factory that is defined in the Spring application context as bean:

    <bean id="connectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
      <property name="brokerURL" value="tcp://localhost:61616" />
    </bean>
    
We use ActiveMQ as message broker so we use the respective connection factory implementation here. The message broker is automatically
started with the Maven build lifecycle.
    
No we can send some content as binary message to the JMS queue destination.
    
    send(todoJmsEndpoint)
        .messageType(MessageType.BINARY)
        .message(new DefaultMessage("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }".getBytes()));
        
The sample uses the **getBytes** method of Java String class in order to get binary content as byte array. Citrus will automatically
take care on this binary content by creating a binary JMS message.

Now the next step is to receive the same binary message in Citrus in order to do some validation. We can receive the binary message content
by marking the message type as **BINARY**. As binary content is not comparable we use a special message validator implementation that converts the
binary content to a String representation for comparison.

    receive(todoJmsEndpoint)
        .messageType(MessageType.BINARY)
        .validator(new BinaryMessageValidator())
        .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");
        
The binary message validator implementation is very simple and performs String equals for validation:
        
    private class BinaryMessageValidator extends AbstractMessageValidator<DefaultValidationContext> {
        @Override
        public void validateMessage(Message receivedMessage, Message controlMessage,
                                    TestContext context, DefaultValidationContext validationContext) {
            Assert.isTrue(new String(receivedMessage.getPayload(byte[].class))
                    .equals(new String(controlMessage.getPayload(byte[].class))), "Binary message validation failed!");
        }
    
        @Override
        public boolean supportsMessageType(String messageType, Message message) {
            return messageType.equalsIgnoreCase(MessageType.BINARY.name());
        }
    
        @Override
        protected Class getRequiredValidationContextType() {
            return DefaultValidationContext.class;
        }
    }    

This way you can implement your own validation as you know best how to handle the binary content.

We can also use base64 encoding for handling binary data in Citrus. The base64 encoding can be used to process the binary content
with basic comparison in **BINARY_BASE64** message validator:

    receive(todoJmsEndpoint)
        .messageType(MessageType.BINARY_BASE64)
        .payload("citrus:encodeBase64('{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }')");
        
Just use the encodeBase64 function in Citrus to provide the expected payload content. Citrus will automatically convert the received 
binary content to base64 encoded Strings then for you. 
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install -Dembedded=true
    
This executes the complete Maven build lifecycle. The embedded option automatically starts an ActiveMQ message broker 
before the integration test phase. This is everything we need for this sample as Citrus is both message producer and consumer
at the same time.

During the build you will see Citrus performing some integration tests.
After the tests are finished the ActiveMQ broker is automatically stopped.

Citrus test
---------

You can execute the test cases in this sample via command line terminal.

Execute all Citrus tests by calling

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output and of course green tests at the very 
end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/index.html#jms
