Citrus samples ![Logo][1]
==============

Greeting sample
---------
  
The greeting sample project is separated into two parts each one handling another type of
message transport. One part is dealing with simple JMS messaging (synchronous). The other part
is handling messages on Spring Integration message channels. The application receives greeting
requests messages and creates proper greeting responses according to the chosen message
transport (JMS or message channel).
  
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

## YAML and XML test variants

Besides the Java integration tests this sample ships declarative ports: `GreetingJms_IT.citrus.it.yaml`, `GreetingChannel_IT.citrus.it.yaml` + `GreetingJms_IT.citrus.it.xml`, `GreetingChannel_IT.citrus.it.xml` (same scenarios), executed via `GreetingYamlIT` / `GreetingXmlIT` (`@CitrusTestSource`). Run all variants: `mvn verify`. Run only ports: `mvn verify -Dit.test=GreetingYamlIT,GreetingXmlIT`.

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/

## Sample contract

- **Purpose:** Test greeting flows over JMS and Spring Integration channels.
- **Modules:** `showcase-greeting`; copy the complete showcase root.
- **Runtime:** TestNG (`testng`).
- **DSLs:** Java, XML, YAML.
- **Prerequisites:** JDK 21+ and Maven 3.9+ for a copied Maven sample.
- **Citrus version:** 5.1.0, declared in `pom.xml`.

Run from the repository root:

```shell
./mvnw -f showcases/greeting/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

Copy this complete sample root outside the repository. No aggregator parent installation is required:

```shell
cp -R showcases/greeting /tmp/citrus-showcases-greeting
cd /tmp/citrus-showcases-greeting
mvn clean verify -Dsystem.under.test.mode=embedded
```
