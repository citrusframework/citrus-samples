Citrus Cucumber BDD sample ![Logo][1]
==============

Sample application
---------

The sample uses Cucumber behavior driven development (BDD) library. The tests combine BDD feature stories with the famous 
Gherkin syntax and Citrus integration test capabilities.
 
Objectives
---------

This sample application shows the usage of both Cucumber and Citrus in combination. The sample also uses Cucumber Spring
support in order to inject autowired beans to step definitions. The step definitions add *@ContextConfiguration(classes = CitrusSpringConfig.class)*
annotation in order to load the Citrus Spring applicaiton context with Cucumber Spring support.

All bean definitions ready for dependency injection using autowiring are located in the default Citrus Spring configuration file
*classpath:citrus-context.xml*. This way step definitions can use *@Autowired* annotations.

Get started
---------

As a starting point look at the test sources of this project. You will find a feature test using JUnit and Cucumber runner.
Also you may look at the step definitions where autowiring takes place and Citrus test designer is used to build integration test logic.

Configuration
---------

There are some configuration aspects that should be highlighted in particular. The sample uses Cucumber Spring support. Therefore
we have included the respective Maven dependency to the project:

    <dependency>
      <groupId>info.cukes</groupId>
      <artifactId>cucumber-spring</artifactId>
    </dependency>
    
Secondly we choose Citrus Spring object factory in *cucumber.properties* in order to enable Cucumber Spring support in all tests.
    
    cucumber.api.java.ObjectFactory=cucumber.runtime.java.spring.CitrusSpringObjectFactory
    
These two steps are required to make Citrus work with Cucumber Spring features.    

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/