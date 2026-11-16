Todo sample application ![Logo][1]
==============

This is a very simple sample application that represents the system under test for the Citrus samples. This
todo application provides an API for adding, removing and changing todo entries.

Objectives
---------

The todo-list sample application should be both simple but yet capable to act as service in a fully qualified integration test
scenario. The application provides a REST API for accessing the todo entries via Http.
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install
    
This executes the complete Maven build lifecycle and creates the sample artifacts that are used throughout the other samples.
The build application represents the system under test.

System under test
---------

The application is a Spring Boot web application that you can deploy on any web container. Of course Spring Boot provides many other fantastic
ways of starting the application.

On of these possibilities is the Spring Boot Maven Plugin. You can start the sample todo list application with this command.

     mvn spring-boot:run

This starts the application in a Tomcat web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list. Now add some new todo entries manually and you are ready to go.

Now we are ready to execute some Citrus tests. Choose on of the Citrus sample folders and read the instructions on how to interact with the
todo application.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#validation-xhtml
