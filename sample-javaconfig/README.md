Java config sample ![Logo][1]
==============

This sample uses pure Java POJOs as configuration.

Objectives
---------

Citrus uses Spring Framework as glue for everything. Following from that Citrus components are
defined as Spring beans in an application context. You can use XML
configuration files and you can also use Java POJOs.

This sample uses pure Java code for both Citrus configuration and tests. The
Citrus TestNG test uses a context configuration annotation.

    @ContextConfiguration(classes = { EndpointConfig.class })
    
This tells Spring to load the configuration from the Java class ***EndpointConfig***.
    
    @Bean
    public HttpClient todoListClient() {
        return CitrusEndpoints.http()
                    .client()
                    .requestUrl("http://localhost:8080")
                    .build();
    }
    
In the configuration class we are able to define Citrus components for usage in tests. As usual
we can autowire the Http client component as Spring bean in the test cases.
    
    @Autowired
    private HttpClient todoListCLient;
     
Secondly we can use the ***CitrusEndpoint*** annotation to automatically create a new endpoint component in a test.
    
    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8080")
    private HttpClient todoClient;
    
In contrast to adding the bean to the Spring application context we define the endpoint using annotation configurations.    
        
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
 [4]: http://www.citrusframework.org/reference/html/index.html#validation-xhtml
