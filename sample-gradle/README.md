Gradle sample ![Logo][1]
==============

This sample uses Gradle as build tool in order to execute the Citrus tests.

Objectives
---------

Citrus uses Maven internally for building software. But of course you can also integrate the Citrus tests in a Gradle
project. As the Citrus tests are nothing but normal JUnit or TestNG tests the integration in the Gradle build is very easy.

The Gradle build configuration is done in the **build.gradle** and **settings.gradle** files. Here we define the project name 
and the project version.

```groovy
rootProject.name = 'citrus-sample-gradle'
group 'com.consol.citrus.samples'
version '2.7.1'
```
    
Now as Citrus libraries are available on Maven central repository we add these repositories so Gradle knows how to download the required
Citrus artifacts.    
    
```groovy
repositories {
    mavenCentral()
    maven {
        url 'http://labs.consol.de/maven/snapshots-repository/'
    }
}
```
    
Citrus stable release versions are available on Maven central. If you want to use the very latest next version as snapshot preview you need
to add the ConSol Labs snapshot repository which is optional. Now lets move on with adding the Citrus libraries to the project.
    
```groovy
dependencies {
    testCompile group: 'com.consol.citrus', name: 'citrus-core', version: '2.7.4-SNAPSHOT'
    testCompile group: 'com.consol.citrus', name: 'citrus-java-dsl', version: '2.7.4-SNAPSHOT'
    testCompile group: 'org.testng', name: 'testng', version: '6.11'
    [...]
}
```
    
This enables the Citrus support for the project so we can use the Citrus classes and APIs. We decided to use TestNG unit test library.
    
```groovy
test {
    useTestNG()
}
```
    
Of course JUnit is also supported. This is all for build configuration settings. We can move on to writing some Citrus integration tests. You can
find those tests in **src/test/java** directory.

This sample uses pure Java code for both Citrus configuration and tests. The
Citrus TestNG test uses a context configuration annotation.

```java
@ContextConfiguration(classes = { EndpointConfig.class })
```
    
This tells Spring to load the configuration from the Java class ***EndpointConfig***.
    
```java
public class EndpointConfig {
    @Bean
    public ChannelEndpoint testChannelEndpoint() {
        ChannelEndpointConfiguration endpointConfiguration = new ChannelEndpointConfiguration();
        endpointConfiguration.setChannel(testChannel());
        return new ChannelEndpoint(endpointConfiguration);
    }

    @Bean
    private MessageChannel testChannel() {
        return new MessageSelectingQueueChannel();
    }
}
```
    
In the configuration class we are able to define Citrus components for usage in tests. As usual
we can autowire the endpoint components as Spring beans in the test cases.
    
```java
@Autowired
private ChannelEndpoint testChannelEndpoint;
```
        
Run
---------

The sample application uses Gradle as build tool. So you can use the Gradle wrapper for compile, package and test the
sample with Gradle build.
 
     gradlew clean build
    
This executes all Citrus test cases during the build and you will see Citrus performing some integration test logging output.
After the tests are finished build is successful and you are ready to go for writing some tests on your own.

If you just want to execute all tests you can call

    gradlew clean check

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the Gradle integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
