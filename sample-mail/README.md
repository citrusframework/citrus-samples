Mail sample ![Logo][1]
==============

This sample demonstrates the usage of mail server activities in Citrus tests. You can also read about this in [reference guide][4].

Objectives
---------

The [todo-list](../todo-app/README.md) sample application sends out mail reports to users on demand.
Citrus is able to trigger the report via Http REST API. In this sample we send out some Http REST calls and
wait for the incoming mail in a single test.

First we need the mail server component in Citrus. Lets add this to the configuration:

    <citrus-mail:server id="mailServer"
                port="2222"
                auto-accept="true"
                auto-start="true"/>
                
The mail server component needs a special XML namespace in the configuration root element.
                
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:citrus-mail="http://www.citrusframework.org/schema/mail/config"
           xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.citrusframework.org/schema/mail/config http://www.citrusframework.org/schema/mail/config/citrus-mail-config.xsd">            
    
Now we can receive the mail in the test case.
    
    receive(mailServer)
        .payload(new ClassPathResource("templates/mail.xml"))
        .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report");

    send(mailServer)
        .payload(new ClassPathResource("templates/mail-response.xml"));            
        
The mail content is loaded from external file resource. Here is the mail content that we expect to arrive in the test.

    <mail-message xmlns="http://www.citrusframework.org/schema/mail/message">
      <from>todo-report@example.org</from>
      <to>users@example.org</to>
      <cc></cc>
      <bcc></bcc>
      <subject>ToDo report</subject>
      <body>
        <contentType>text/plain; charset=us-ascii</contentType>
        <content>There are '${entryCount}' todo entries!</content>
      </body>
    </mail-message>
        
Citrus is able to convert mail messages to an internal XML representation. This way the content is more comfortable to
compare in validation. The mail response looks like this.

    <mail-response xmlns="http://www.citrusframework.org/schema/mail/message">
      <code>250</code>
      <message>OK</message>
    </mail-response>
    
In the sample the success code **250** is returned to the mail client marking that everything is ok. Here we also could place
some other code and message in order to simulate mail server problems.
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
    > mvn clean install -Dembedded=true
    
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

    > mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

> mvn integration-test

You can also pick a single test by calling

> mvn integration-test -Ptest=TodoListIT

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
 [4]: http://www.citrusframework.org/reference/html/index.html#mail
