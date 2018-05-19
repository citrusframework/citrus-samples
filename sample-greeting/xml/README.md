Citrus samples ![Logo][1]
==============

Greeting sample
---------
  
The greeting sample project is separated into two parts each one handling another type of
message transport. One part is dealing with simple JMS messaging (synchronous). The other part
is handling messages on Spring Integration message channels. The application receives greeting
requests messages and creates proper greeting responses according to the chosen message
transport (JMS or message channel).
  
Server
---------

Got to the war folder and start the BookStore WebService application in a Web Container. Easiest
way for you to do this is to execute

     mvn jetty:run

here!

An embedded Jetty Web Server Container is started with the BookStore application deployed. You can
alsp call "mvn package" and deploy the resulting war archive to a separate Web container of your choice.
  
Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases in citrus-test folder.
Open a separate command line terminal and navigate to the citrus-test folder.

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
