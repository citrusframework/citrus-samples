Kubernetes sample ![Logo][1]
==============

This sample uses [Kubernetes](https://kubernetes.io/) as container platform and deploys both application and integration tests as Pods in Kubernetes.
The Citrus integration tests may then access the Kubernetes exposed services and APIs of the system under test.

Read about the Citrus Kubernetes integration in the [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries. We want to deploy the
whole application in Kubernetes in order to expose the REST API as a service to other Pods running on the Kubernetes platform. The Citrus tests
get deployed to Kubernetes as Pod, too. The deployed Citrus tests are then able to connect with the todo-list REST API via service discovery 
in Kubernetes.

Prepare the environment
---------

First of all you need a running Kubernetes platform. [Minikube](https://github.com/kubernetes/minikube) is a fantastic way to get started with a 
local Kubernetes cluster for testing. Please refer to the [installation](https://github.com/kubernetes/minikube/releases) description on how to 
set up the Minikube environment.

Or you can simply call the [Fabric8 Maven plugin](https://maven.fabric8.io/) to setup everything for you:

```
mvn fabric8:start-cluster
```

This command will download and install Minikube on your local system.

Deploy Pods to Kubernetes
---------

The sample uses the [Fabric8 Maven plugins](https://maven.fabric8.io/) for Kubernetes resource generation and deployment. 

First of all we build and deploy the todo-list application to Kubernetes. The todo-list project Maven POM uses plugin configurations for Fabric8 
in order to define a deployment with a service and a pod running within a replication set. The Fabric8 Maven plugin configuration looks like follows.
  
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

The configuration looks quite large but if we have a closer look at the different aspects everything is straight forward. First of all the configuration defines a Docker image *citrus/todo-app*
with a build configuration. As the todo-list application is a Java Spring Boot web application we use a Tomcat web server when running the application as Docker container. 
So the Docker image extends from *fabric8/tomcat-8:latest* which is provided by the Fabric8 team. This Docker image is best suited for working with
artifacts that are assembled within Maven.
                             
In the assembly section we define the target WAR that should be deployed to the Tomcat web server in the Docker image. That is very comfortable as the assembly can be done in various ways 
(also see [Fabric8 Docker assembly](https://dmp.fabric8.io/#build-assembly)). In this sample above we just copy the built todo-list WAR from our local Maven repository. 
 
In addition to the Docker image the configuration defines a service *citrus-sample-todo-service* that exposes the API with type *NodePort* on port *8080*. This means that other pods will be 
able to access the service via `http://citrus-sample-todo-service:8080`.
       
In our tests we will be able to use this service on the internal port as the Citrus tests are also deployed to the Kubernetes platform. We will see this later in more detail.

With that configuration we can execute the Fabric8 build and deployment process via:

```
mvn clean package fabric8:resource fabric8:build fabric8:deploy
```
     
This builds the Kubernetes resources as well as the Docker Tomcat 8 image with automatic deployment to your local Minikube Kubernetes cluster.

To be continued ...
     
Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/kubernetes.html
