ToDo sample application ![Logo][1]
==============

This is a very simple sample application that represents the system under test in the Citrus samples. This
ToDo list application provides several inbound channels for adding, removing and changing todo entries.

Objectives
---------

The ToDo sample application should be both simple but yet capable to act as service in a fully qualified integration test
scenario.
        
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
    > mvn clean install
    
This executes the complete Maven build lifecycle and creates the sample artifacts that are used throughout the other samples.
The build application represents the system under test.

System under test
---------

The application is a web application that you can deploy on any web container.  

You can start the sample todo list application in Jetty with this command.

    > mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests. Go to one of the sample subfolders and read the instructions on how to interact with the
system under test.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/index.html#validation-xhtml
