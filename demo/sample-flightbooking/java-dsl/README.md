Citrus samples ![Logo][1]
==============

Flightbooking sample
---------

The FlightBooking sample application receives request messages from a travel agency over JMS (async). The application splits the request into several flight bookings and forwards the messages to respective airline applications (Http or JMS). The consolidated response message is sent back to the calling travel agency asynchronous over JMS.

The test cases contain simple workflows for handling `TravelBookingRequest` messages with several flight bookings. See the log output for detailed information how Citrus validates the received messages.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the sample with Maven:
 
```
mvn clean verify -Dsystem.under.test.mode=embedded
```

This executes the Maven build lifecycle until phase `verify` which includes the `integration-test` and its `pre-` and `post-` phases. The `embedded` option automatically starts an in-memory Jetty web container and an ActiveMQ message broker during the `pre-integration-test` phase. The flight booking system under test is automatically deployed in this phase. After that the Citrus test cases are able to interact with the application during the `integration-test` phase.

During the build you will see Citrus performing some integration tests. After the tests are finished the embedded ActiveMQ broker and the Jetty web container including the flight booking application are automatically stopped.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
