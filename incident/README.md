Citrus samples ![Logo][1]
==============

Incident Manager sample
---------

The IncidentManager sample application offers a SOAP WebService with following supported
operations:

* addBook
* getBookDetails
* listBooks

Each operation will result in a synchronous SOAP response to the calling client. Duplicate
books (isbn) or unknown books will generate SOAP Faults in the response. The different sample
test cases will call the WebService as client and test the complete functionality for the
available operations.

Server
---------

Got to the war folder and start the IncidentManager WebService application in a Web Container. Easiest
way for you to do this is to execute

> mvn jetty:run

here!

An embedded Jetty Web Server Container is started with the IncidentManager application deployed. You can
alsp call "mvn package" and deploy the resulting war archive to a separate Web container of your choice.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases in citrus-test folder.
Open a separate command line terminal and navigate to the citrus-test folder.

Execute all Citrus tests by calling

> mvn integration-test

You can also pick a single test by calling

> mvn integration-test -Ptest=TestName

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

You can also use Apache ANT to execute the tests. Run the following command to see which targets are offered:

> ant -p

Buildfile: build.xml

Main targets:

citrus.run.single.test  Runs a single test by name
citrus.run.tests        Runs all Citrus tests
create.test             Creates a new empty test case
Default target: citrus.run.tests

The different targets are not very difficult to understand. You can run all tests, a single test case by its name or create
new test cases.

Just try to call the different options like this:

> ant citrus.run.tests

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/images/brand_logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/