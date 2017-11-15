Kubernetes sample ![Logo][1]
==============

This sample uses [Kubernetes](https://kubernetes.io/) as container platform and deploys both application under test and integration tests as pods in Kubernetes.
The Citrus integration tests may then access the Kubernetes exposed services and APIs within the test.

Read about the Citrus Kubernetes integration in the [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries. We want to deploy the
whole application in Kubernetes in order to expose the REST API to other pods running on the Kubernetes platform. The Citrus tests
will also get deployed to Kubernetes in order to access the exposed service API via Kubernetes service discovery. In addition to that we access the Kubernetes
REST services within the Citrus tests in order to verify deployment states.

Prepare the environment
---------

First of all you need a running Kubernetes platform. [Minikube](https://github.com/kubernetes/minikube) is a fantastic way to get started with a 
local Kubernetes cluster for testing. Please refer to the [installation](https://kubernetes.io/docs/getting-started-guides/minikube/) description on how to 
set up the Minikube environment.

Or you can simply call the [Fabric8 Maven plugin](https://maven.fabric8.io/) to setup everything for you:

```
mvn fabric8:start-cluster
```

This command will download all needed tools and install Minikube on your local system. After that you should be able to access the Kubernetes dashboard
with:

```
minikube dashboard
```

Deploy pods to Kubernetes
---------

This sample uses the [Fabric8 Maven plugins](https://maven.fabric8.io/) for generating the Kubernetes resources with automatic deployment to the local cluster. 

First of all we build and deploy the todo-list application as pod in Kubernetes. We are going to build a Docker image the runs the todo-list application as web 
application in Tomcat 8. In addition to that we define a service resource in Kubernetes that exposes the todo-list REST API to other pods. 

The complete configuration is placed in the todo-list project Maven POM ([pom.xml](../todo-app/pom.xml)) as plugin configuration. The Fabric8 Maven plugin configuration looks like follows.
  
```xml
<plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>fabric8-maven-plugin</artifactId>
    <version>${fabric8.plugin.version}</version>
    <configuration>
      <verbose>true</verbose>
      <mode>kubernetes</mode>
      <images>
        <image>
          <alias>todo-app</alias>
          <name>citrus/todo-app:${project.version}</name>
          <build>
            <from>fabric8/tomcat-8:latest</from>
            <tags>
              <tag>latest</tag>
            </tags>
            <assembly>
              <inline>
                <files>
                  <file>
                    <source>${settings.localRepository}/com/consol/citrus/samples/citrus-sample-todo/${project.version}/citrus-sample-todo-${project.version}.war</source>
                    <destName>ROOT.war</destName>
                    <outputDirectory>.</outputDirectory>
                  </file>
                </files>
              </inline>
            </assembly>
          </build>
        </image>
      </images>
      <resources>
        <services>
          <service>
            <name>citrus-sample-todo-service</name>
            <ports>
              <port>
                <protocol>tcp</protocol>
                <port>8080</port>
                <targetPort>8080</targetPort>
              </port>
            </ports>
            <type>NodePort</type>
          </service>
        </services>
      </resources>
    </configuration>
</plugin>
```

The configuration is much more straight forward than it looks like at first sight. First of all the configuration defines a Docker image *citrus/todo-app* build section. 
The todo-list Spring Boot web application should be deployed on a Tomcat web server. So the Docker image extends from *fabric8/tomcat-8* base image which is also provided by the Fabric8 team. 
This Docker image is best suited for working with artifacts that are assembled within the Maven build.
                             
In the assembly section we define the target WAR that should be deployed to the Tomcat web server in the Docker image. This is very comfortable way of adding artifacts and other sources to the web
server as the assembly can be done in various ways (also see [Fabric8 Docker assembly](https://dmp.fabric8.io/#build-assembly)). In this sample above we just copy the todo-list WAR from our local 
Maven repository. 
 
In addition to that the configuration also defines a service *citrus-sample-todo-service* that exposes the port *8080*. This means that other pods will be 
able to access the todo-list service via `http://citrus-sample-todo-service:8080`. The type *NodePort* will also create a random port for external clients that are coming from outside of Kubernetes. 
That completes the configuration on the Fabric8 plugin. The plugin reads this information and creates all Kubernetes resource configurations for us. Lets do that by calling

```
mvn fabric8:resource
mvn install fabric8:build
```

You can review the Kubernetes configurations for the complete deployment in Maven *target/classes/META-INF/fabric8/* folder now. We are now ready to deploy the todo-list application to Kubernetes.

```
mvn fabric8:deploy
```
     
After that you should see a new deployment on your local Minikube Kubernetes cluster. Also you should be able to see the exposed Kubernetes service *citrus-sample-todo-service* with a default
replica set and of course the running pod with the the Tomcat 8 todo-list application.

The node port service type generated an external port for us. So we can also access the todo-list application with the browser.

```
minikube service citrus-sample-todo-service
```

This opens you local browser pointing to the external node port. You should be able to see the todo-list application now.

Run Citrus tests as pod
---------

Now that we have a running Kubernetes deployment we also want to execute the Citrus integration tests as pod in Kubernetes. This has the great advantage that we can access the todo-list REST service
as client in Kubernetes just like every other pod would do in production. The Citrus tests will be able to call operations via the exposed Kubernetes service.
 
Lets review the Fabric8 Maven plugin configuration for the Citrus integration test POM:
 
```xml
<plugin>
  <groupId>io.fabric8</groupId>
  <artifactId>docker-maven-plugin</artifactId>
  <version>${fabric8.maven.plugin}</version>
  <configuration>
    <verbose>true</verbose>
    <images>
      <image>
        <alias>todo-app-tests</alias>
        <name>citrus/todo-app-tests:${project.version}</name>
        <build>
          <from>consol/citrus:2.7</from>
          <tags>
            <tag>latest</tag>
          </tags>
          <assembly>
            <descriptorRef>project</descriptorRef>
          </assembly>
        </build>
      </image>
    </images>
  </configuration>
</plugin>
``` 

Once again we define a Docker image that is supposed to run as part of a Kubernetes pod. This time we extend from *consol/citrus:2.7* Docker image which is ready to execute a Citrus Maven build at 
container runtime. The image also works with the Docker Maven plugin assembly mechanism. This time the assembly adds the complete project sources by using the *descriptorRef=project* which is a 
predefined assembly in the Fabric8 plugin.

When we build the image with `mvn docker:build` we get a ready to use Citrus Docker image of the current Maven project. When the image is run as pod in Kubernetes all integration tests will be executed
as Maven build.

You might have noticed that we now use the Docker Fabric8 Maven plugin here (`<artifactId>docker-maven-plugin</artifactId>`). This is simply because we do not want Fabric8 to create a Kubernetes deployment for the
Citrus tests. Deployments in Kubernetes are designed for pods that are supposed to never end. The Citrus pod is supposed to run all tests. After that the pod will terminate with success or failure. This
is not a classic deployment in Fabric8 and Kubernetes. Instead we use a customized pod YAML configuration. We simply tell Fabric8 to just create that specific resource in Kubernetes. All
default deployment creation steps are then skipped. Here is the Citrus pod YAML config:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: citrus-sample-todo-tests
spec:
  restartPolicy: Never
  containers:
  - name: citrus-sample-todo-tests
    image: citrus/todo-app-tests:2.7.1
    imagePullPolicy: Never
    securityContext:
      privileged: false
```

The pod configuration tells Kubernetes to use the Docker image *citrus/todo-app-tests:2.7.1* that we have just built. Also the *restartPolicy* is set to *Never*. This makes sure
that Kubernetes will not try to endlessly restart the pod in case a test fails with error.

Now lets put that all together with:

```
mvn docker:build
mvn fabric8:resource
mvn fabric8:deploy -DskipTests
```

We have to skip the tests when calling *fabric8:deploy* because we do not want to execute the tests locally but inside the Kubernetes pod. After that you should see a new pod running 
on your Kubernetes cluster. This pod automatically executes the Citrus tests and terminates. You will see the terminated pod and
you will be able to review the logs. In case all Citrus tests pass the pod terminates with *Terminate: Complete*. In case the tests are broken the pod terminates in state *Terminate: Error*.

Access Kubernetes resources in test
---------

Have a look at the Citrus endpoint configuration that shows the service discovery via Kubernetes:

```xml
<citrus-http:client id="todoListClient"
            request-url="http://citrus-sample-todo-service:8080"/>
```

The client component in Citrus uses the Kubernetes service name *citrus-sample-todo-service* as host. The host and port are automatically resolved as the Citrus tests run as pod in Kubernetes. Also
we can add a Citrus Kubernetes client to access the Kubernetes API within a test:

```xml
<citrus-k8s:client id="k8sClient"
               username="minikube"
               namespace="default"
               url="https://kubernetes:443/"/>
```

The client also uses the Kubernetes internal host and port for Kubernetes exposed services. With this client we can access the running pods and services from within a Citrus test:

```java
@Autowired
private KubernetesClient k8sClient;

@Test
@CitrusTest
public void testDeploymentState() {
    kubernetes()
        .client(k8sClient)
        .pods()
        .list()
        .label("app=todo")
        .validate("$..status.phase", "Running")
        .validate((pods, context) -> {
            Assert.assertFalse(CollectionUtils.isEmpty(pods.getResult().getItems()));
        });

    kubernetes()
        .client(k8sClient)
        .services()
        .get("citrus-sample-todo-service")
        .validate((service, context) -> {
            Assert.assertNotNull(service.getResult());
        });
}
```

As you can see in the sample above we are able to test the deployment state of the todo-list applicaiton within Kubernetes. The test verifies that the pod is running and that
the service is set up correctly. Now we can access the todo-list REST API in another test:

```java
@Autowired
private HttpClient todoClient;

@Test
@CitrusTest
public void testTodoService() {
    variable("todoId", "citrus:randomUUID()");
    variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
    variable("todoDescription", "Description: ${todoName}");
    variable("done", "false");

    http()
        .client(todoClient)
        .send()
        .post("/todolist")
        .messageType(MessageType.JSON)
        .contentType("application/json")
        .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");

    http()
        .client(todoClient)
        .receive()
        .response(HttpStatus.OK)
        .messageType(MessageType.PLAINTEXT)
        .payload("${todoId}");

    http()
        .client(todoClient)
        .send()
        .get("/todo/${todoId}")
        .accept("application/json");

    http()
        .client(todoClient)
        .receive()
        .response(HttpStatus.OK)
        .messageType(MessageType.JSON)
        .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");
}
```

With these integration tests we make sure to access the services and pods as every other pod running in Kubernetes would do. These are in-container tests
that consume services just like other clients would do in production. On top of that we can use the Citrus features such as message validation and service simulation.

The test is able to check the Kubernetes deployment state. We can even manipulate the Kubernetes resources at test runtime:

```java
kubernetes()
    .pods()
    .delete("${todoPod}")
    .namespace("default")
    .validate((result, context) -> Assert.assertTrue(result.getResult().getSuccess()))
```

The listing above deletes the todo-list pod. In that case the default Kubernetes replica set may just automatically start another pod so the todo-list application is kept running.
The todo-list service should always be reachable for clients. We could verify that by constantly adding todo entries as client while deleting the pod. 

We can think of many possibilities and test scenarios with this integration test setup then. 
     
Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html#kubernetes
