Citrus samples ![Logo][1]
==============

Flightbooking sample
---------

The FlightBooking sample application receives request messages from a travel agency over
JMS (async). The application splits the request into several flight bookings and forwards
the messages to respective airline applications (Http or JMS). The consolidated response
message is sent back to the calling travel agency asynchronous over JMS.

The test cases contain simple workflows for handling TravelBookingRequest messages with
several flight bookings. See the log output for detailed information how Citrus validates
the received messages.
  
Server
---------

Got to the folder and start the BookStore WebService application in a Web Container. Easiest
way for you to do this is to execute

> mvn jetty:run

here!

An embedded Jetty Web Server Container is started with the BookStore application deployed. You can
alsp call "mvn package" and deploy the resulting war archive to a separate Web container of your choice.
  
Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

> mvn integration-test

You can also pick a single test by calling

> mvn integration-test -Ptest=TestName

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
