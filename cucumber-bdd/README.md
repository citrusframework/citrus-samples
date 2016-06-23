Citrus Cucumber BDD sample ![Logo][1]
==============

Sample application
---------

The sample uses Cucumber behavior driven development (BDD) library. The tests combine BDD feature stories with the famous 
Gherkin syntax and Citrus integration test capabilities.
 
Objectives
---------

This sample application shows the usage of both Cucumber and Citrus in combination. Step definitions are able to use *CitrusResource*
annotations for injecting a TestDesigner instance. The test designer is then used in steps to build a Citrus integration test.

At the end the Citrus test is automatically executed. We can use normal step definition classes that use Gherkin annotations
(@Given, @When, @Then) provided by Cucumber.

Get started
---------

As a starting point look at the test sources of this project. You will find a feature test using JUnit and Cucumber runner.
Also you may look at the step definitions where Citrus test designer is used to build integration test logic.

Configuration
---------

In order to enable Citrus Cucumber support we need to specify a special object factory in *cucumber.properties*.
    
    cucumber.api.java.ObjectFactory=cucumber.runtime.java.CitrusObjectFactory
    
The object factory takes care on creating all step definition instances. The object factory is able to inject *@CitrusResource*
annotated fields in step classes.
    
The usage of this special object factory is mandatory in order to combine Citrus and Cucumber capabilities. 
   
We also have the usual *citrus-context.xml* Citrus Spring configuration that is automatically loaded within the object factory.
So you can define and use Citrus components as usual within your test. In this sample application we use a little Apache Camel
route to create a very simple business logic.   

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/images/brand_logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/