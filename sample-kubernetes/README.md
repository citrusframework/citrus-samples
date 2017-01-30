Kubernetes sample ![Logo][1]
==============

This sample uses Kubernetes as container platform and deploys both application and integration tests as Pods in Kubernetes.
Read about the Citrus Kubernetes integration in the [reference guide][4]

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries. We want to deploy the
whole application in Kubernetes in order to expose the REST API as a service to other Pods running on the Kubernetes platform. The Citrus tests
get deployed to Kubernetes as Pod, too. The deployed Citrus tests are then able to connect with the todo-list REST API via service discovery 
in Kubernetes.

The sample uses the Fabric8 Maven plugins for Kubernetes resource generation and deployment. First of all we open the todo-list Maven project in
order to deploy the application to Kubernetes. The todo-list application defines a deployment with a service and a pod running within a replication 
set. The Fabric8 Maven plugin configuration looks like follows.
  
```xml
<plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>fabric8-maven-plugin</artifactId>
    <version>3.2.15</version>
    <configuration>
      <mode>kubernetes</mode>
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
      <verbose>true</verbose>
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
    </configuration>
</plugin>
```
We define a service *citrus-sample-todo-service* that exposes the API with type *NodePort* on port *8080*. This means that other pods will be able to access the service via 

    http://citrus-sample-todo-service:8080
       
This endpoint is available to all pods running in Kubernetes as this is the internal service definition. As the service type is *NodePort* Kubernetes will also create a dynamic port that
exposes the service to clients outside of Kubernetes. In our tests we will be using the internal service discovery as the Citrus tests are also deployed to the Kubernetes platform.

The Docker image to build is defined in the *images* section. The image is build on top of the base image *fabric8/tomcat-8* which is a Docker image ready to run any Java web archive (WAR) on
Tomcat 8. We add the .war file as *ROOT.war* so the todo-list application will be ready on Tomcat using the root context */*.

First of all you need a running Kubernetes platform. Minikube is the best way to start a local Kubernetes cluster for testing. Please refer to the description on how to set up the Minikube environment.

Execute the Fabric8 deployment process via:

    mvn clean package fabric8:resource fabric8:build fabric8:deploy
     
This builds the Kubernetes resource configuration, builds the Docker Tomcat 8 image and deploys everything to Kubernetes.

To be continued ...
     
Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/kubernetes.html
