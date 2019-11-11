Http basic auth sample ![Logo][1]
==============

This sample demonstrates the usage of Http basic authentication on client and server. Http support is described in detail in [reference guide][4]

Objectives
---------

In this sample project we want to configure both Http client and server to use basic authentication. On client side we can add the basic authentication header manually in each send operation.

```java
http(httpActionBuilder -> httpActionBuilder
    .client(todoClient)
    .send()
    .get("/todo/")
    .accept(ContentType.APPLICATION_XML.getMimeType())
    .header("Authorization", "Basic citrus:encodeBase64('citrus:secr3t')"));
```
        
The `Authorization` header is holding the username password combination as base64 encoded string. We need to add this header manually to the send operation. The server will verify the username password
before the request is processed. This is an easy way to add basic authentication information to a request in Citrus. On the downside we have to manually add the authentication header in each send operation.

Fortunately we can also add the basic authentication to the client component. So all requests with this client will automatically add the proper authentication header. We need a special Http client configuration for that:

```java
@Bean
public HttpClient todoBasicAuthClient() throws Exception {
    return CitrusEndpoints
        .http()
            .client()
            .requestUrl("http://localhost:" + port)
            .requestFactory(basicAuthRequestFactory())
        .build();
}

@Bean
public BasicAuthClientHttpRequestFactory basicAuthRequestFactoryBean() {
    BasicAuthClientHttpRequestFactory requestFactory = new BasicAuthClientHttpRequestFactory();

    AuthScope authScope = new AuthScope("localhost", port, "", "basic");
    requestFactory.setAuthScope(authScope);

    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("citrus", "secr3t");
    requestFactory.setCredentials(credentials);
    return requestFactory;
}

@Bean
public HttpComponentsClientHttpRequestFactory basicAuthRequestFactory() throws Exception {
    return basicAuthRequestFactoryBean().getObject();
}
```
    
The client component references a special request factory of type `BasicAuthClientHttpRequestFactory`. The request factory receives the username password credentials and is defined as bean in the
Spring configuration. Now all send operations that reference this client component will automatically use basic authentication. 
    
On the server side the configuration looks like follows:
        
```java
@Bean
public HttpServer basicAuthHttpServer() throws Exception {
    return CitrusEndpoints
        .http()
            .server()
            .port(port)
            .endpointAdapter(staticEndpointAdapter())
            .securityHandler(basicAuthSecurityHandler())
            .autoStart(true)
        .build();
}     

@Bean
public SecurityHandlerFactory basicAuthSecurityHandlerFactoryBean() {
    SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
    securityHandlerFactory.setUsers(users());
    securityHandlerFactory.setLoginService(basicAuthLoginService(basicAuthUserStore()));
    securityHandlerFactory.setConstraints(Collections.singletonMap("/todo/*", new BasicAuthConstraint(USER_ROLES)));

    return securityHandlerFactory;
}

@Bean
public SecurityHandler basicAuthSecurityHandler() throws Exception {
    return basicAuthSecurityHandlerFactoryBean().getObject();
}
```
        
The server component references a special **security-handler** bean of type `SecurityHandlerFactory`. The security handler also uses a user definition with username password credentials as well as a `BasicAuthConstraint`. 
Clients now have to use the basic authentication in order to connect with this server. Unauthorized requests will be answered with `401 Unauthorized`.
       
The server component has a static endpoint adapter always sending back a Http 200 Ok response when clients connect.

```java
@Bean
public StaticEndpointAdapter staticEndpointAdapter() {
    return new StaticEndpointAdapter() {
        @Override
        protected Message handleMessageInternal(Message message) {
            return new HttpMessage("<todo xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<id>100</id>" +
                        "<title>todoName</title>" +
                        "<description>todoDescription</description>" +
                    "</todo>")
                    .status(HttpStatus.OK);
        }
    };
}
```
       
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dsystem.under.test.mode=embedded
    
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

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#http
