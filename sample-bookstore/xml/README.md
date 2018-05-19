Citrus samples ![Logo][1]
==============

Bookstore sample
---------

The BookStore sample application offers a SOAP WebService with following supported
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
