Citrus samples ![Logo][1]
==============

Camel Bakery Demo
---------

The Camel bakery demo sample application offers a multi module Maven project with each module being deployed in a separate
Docker container. The modules are:

* web
* worker
* report
* integration

Using Docker
---------

This sample is using Docker as infrastructure for starting up the services in separate containers. As a prerequisite you must
have Docker locally installed. Non linux users might add dockerhost to their /etc/hosts configuration in order to simply access the
services running in Docker containers without any port forwarding:

```
echo $(docker-machine ip your-docker-machine-name) dockerhost | sudo tee -a /etc/hosts
```

Now you can build the Docker containers by calling

```
mvn clean package docker:build
```

Now you will be able to see some more docker images

```
docker images
```

Lets start the complete Docker container infrastructure

```
mvn -pl integration docker:start
```

You will then see some Docker containers startet on your host

```
docker ps
```

Now execute some Citrus integration tests

```
mvn -pl integration integration-test
```

To stop the Docker containers run

```
mvn -pl integration docker:stop
```

Now lets run the complete lifecycle with all modules build, shipped to Docker and all Citrus tests executed

```
mvn clean install -Pdocker
```

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases in module folder.
Open a separate command line terminal and navigate to the citrus-test folder.

Execute all Citrus tests by calling

> mvn integration-test

You can also pick a single test by calling

> mvn integration-test -Ptest=TestName

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/images/brand_logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/