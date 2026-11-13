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
rootProject.name = 'citrus-sample-build-tools-gradle'
group 'org.citrusframework.samples'
version '4.0.0'
```
    
Now as Citrus libraries are available on Maven central repository we add these repositories so Gradle knows how to download the required
Citrus artifacts.    
    
```groovy
repositories {
    mavenCentral()
}
```
    
Citrus stable release versions are available on Maven central. If you want to use the very latest next version as snapshot preview you need
to add the ConSol Labs snapshot repository which is optional. Now lets move on with adding the Citrus libraries to the project.
    
```groovy
dependencies {
 testImplementation group: 'org.citrusframework', name: 'citrus-base', version: citrusVersion
 testImplementation group: 'org.citrusframework', name: 'citrus-spring', version: citrusVersion
 testImplementation group: 'org.citrusframework', name: 'citrus-testng', version: citrusVersion
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

The sample application uses Gradle as build tool. Use an installed Gradle 9.x command to compile, package, and test the sample; this project does not include a Gradle wrapper.

     gradle clean build
    
This executes all Citrus test cases during the build and you will see Citrus performing some integration test logging output.
After the tests are finished build is successful and you are ready to go for writing some tests on your own.

If you just want to execute all tests you can call

    gradle clean check

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the Gradle integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

## YAML and XML test variants

Besides the Java integration tests this sample ships declarative ports: `messaging.citrus.it.yaml` + `messaging.citrus.it.xml` (same scenario), executed via `MessagingYamlIT` / `MessagingXmlIT` (`@CitrusTestSource`). Run all variants: `gradle test` (no failsafe fork config). Run only ports: `gradle test --tests MessagingYamlIT --tests MessagingXmlIT`.

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/

## Sample contract

- **Purpose:** Run equivalent Citrus scenarios with a standalone Gradle build.
- **Modules:** `gradle`.
- **Runtime:** TestNG through Gradle (`gradle`).
- **DSLs:** Java, XML, YAML.
- **Prerequisites:** JDK 21+ and an installed Gradle command are required; Maven is not used.
- **Citrus version:** 5.1.0, declared in `gradle.properties`.

Run from the repository root:

```shell
cd build-tools/gradle && gradle clean build
```

Copy this complete sample root outside the repository. No aggregator parent installation is required:

```shell
cp -R build-tools/gradle /tmp/citrus-build-tools-gradle
cd /tmp/citrus-build-tools-gradle
gradle clean build
```
