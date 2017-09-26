Http basic auth sample ![Logo][1]
==============

This sample demonstrates the usage of Http basic authentication on client and server. Http support is described in detail in [reference guide][4]

Objectives
---------

In this sample project we want to configure both Http client and server to use basic authentication. On client side we can add the basic authentication header manually in each send operation.

    http()
        .client(todoClient)
        .send()
        .get("/todo")
        .accept("application/xml")
        .header("Authorization", "Basic citrus:encodeBase64('citrus:secr3t')");
        
The `Authorization` header is holding the username password combination as base64 encoded string. We need to add this header manually to the send operation. The server will verify the username password
before the request is processed. This is an easy way to add basic authentication information to a request in Citrus. On the downside we have to manually add the authentication header in each send operation.

Fortunately we can also add the basic authentication to the client component. So all requests with this client will automatically add the proper authentication header. We need a special Http client configuration for that:

    <citrus-http:client id="todoBasicAuthClient"
                          request-url="http://localhost:8090"
                          request-factory="basicAuthFactory"/>
    
    <bean id="basicAuthFactory"
          class="com.consol.citrus.http.client.BasicAuthClientHttpRequestFactory">
      <property name="authScope">
        <bean class="org.apache.http.auth.AuthScope">
          <constructor-arg value="localhost"/>
          <constructor-arg value="8090"/>
          <constructor-arg value=""/>
          <constructor-arg value="basic"/>
        </bean>
      </property>
      <property name="credentials">
        <bean class="org.apache.http.auth.UsernamePasswordCredentials">
          <constructor-arg value="citrus"/>
          <constructor-arg value="secr3t"/>
        </bean>
      </property>
    </bean>
    
The client component references a special request factory of type `BasicAuthClientHttpRequestFactory`. The request factory receives the username password credentials and is defined as bean in the
Spring configuration. Now all send operations that reference this client component will automatically use basic authentication. 
    
On the server side the configuration looks like follows:
        
    <citrus-http:server id="basicAuthHttpServer"
                            port="8090"
                            endpoint-adapter="staticResponseAdapter"
                            security-handler="basicAuthSecurityHandler"
                            auto-start="true"/>
    
    <bean id="basicAuthSecurityHandler" class="com.consol.citrus.http.security.SecurityHandlerFactory">
      <property name="users">
        <list>
          <bean class="com.consol.citrus.http.security.User">
            <property name="name" value="citrus"/>
            <property name="password" value="secr3t"/>
            <property name="roles" value="CitrusRole"/>
          </bean>
        </list>
      </property>
      <property name="constraints">
        <map>
          <entry key="/todo/*">
            <bean class="com.consol.citrus.http.security.BasicAuthConstraint">
              <constructor-arg value="CitrusRole"/>
            </bean>
          </entry>
        </map>
      </property>
    </bean>        
        
The server component references a special **security-handler** bean of type `SecurityHandlerFactory`. The security handler also uses a user definition with username password credentials as well as a `BasicAuthConstraint`. 
Clients now have to use the basic authentication in order to connect with this server. Unauthorized requests will be answered with `401 Unauthorized`.
       
The server component has a static endpoint adapter always sending back a Http 200 Ok response when clients connect.

    <citrus:static-response-adapter id="staticResponseAdapter">
      <citrus:payload>
        <![CDATA[
        <todo xmlns="http://citrusframework.org/samples/todolist">
          <id>100</id>
          <title>todoName</title>
          <description>todoDescription</description>
        </todo>
        ]]>
      </citrus:payload>
    </citrus:static-response-adapter>
       
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: http://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: http://www.citrusframework.org
 [3]: http://www.citrusframework.org/reference/html/
 [4]: http://www.citrusframework.org/reference/html/http.html
