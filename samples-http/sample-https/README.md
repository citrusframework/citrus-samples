Https sample ![Logo][1]
==============

This sample demonstrates the usage of secure Http connections with client and server certificates. Http support is described in detail in [reference guide][4]

Objectives
---------

In this sample project we want to configure both Http client and server to use secure connections with SSL. First we need a keystore that holds the
supported certificates. The sample uses the keystore in **src/test/resources/keys/citrus.jks**

We need a special Http client configuration:

```java
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints
        .http()
            .client()
            .requestUrl("https://localhost:" + securePort)
            .requestFactory(sslRequestFactory())
        .build();
}
```
    
The client component references a special request factory and uses the transport scheme **https** on port **8443**. The SSL request factory is defined in a
Java Spring configuration class simply because it is way more comfortable to do this in Java than in XML.
    
```java
@Bean
public org.apache.http.client.HttpClient httpClient() {
    try {
        SSLContext sslcontext = SSLContexts
            .custom()
                .loadTrustMaterial(new ClassPathResource("keys/citrus.jks").getFile(), "secret".toCharArray(),
                        new TrustSelfSignedStrategy())
            .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslcontext, NoopHostnameVerifier.INSTANCE);

        return HttpClients
            .custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build();
    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
        throw new BeanCreationException("Failed to create http client for ssl connection", e);
    }
}

@Bean
public HttpComponentsClientHttpRequestFactory sslRequestFactory() {
    return new HttpComponentsClientHttpRequestFactory(httpClient());
}
```
        
As you can see we load the keystore file **keys/citrus.jks** in order to setup the http client ssl context. In the Citrus test case you can use the client component as usual for 
sending messages to the server.

```java
$(http()
    .client(todoClient)
    .send()
    .get("/todo")
    .accept(ContentType.APPLICATION_XML.getMimeType()));

$(http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK));    
```
        
On the server side the configuration looks like follows:
        
```java
@Bean
public HttpServer todoSslServer() throws Exception {
    return CitrusEndpoints
        .http()
            .server()
            .port(8080)
            .endpointAdapter(staticEndpointAdapter())
            .connector(sslConnector())
            .autoStart(true)
        .build();
}

@Bean
public ServerConnector sslConnector() {
    ServerConnector connector = new ServerConnector(new Server(),
            new SslConnectionFactory(sslContextFactory(), "http/1.1"),
            new HttpConnectionFactory(httpConfiguration()));
    connector.setPort(securePort);
    return connector;
}

private HttpConfiguration httpConfiguration() {
    HttpConfiguration parent = new HttpConfiguration();
    parent.setSecureScheme("https");
    parent.setSecurePort(securePort);
    HttpConfiguration configuration = new HttpConfiguration(parent);
    configuration.setCustomizers(Collections.singletonList(new SecureRequestCustomizer()));
    return configuration;
}

private SslContextFactory sslContextFactory() {
    SslContextFactory contextFactory = new SslContextFactory();
    contextFactory.setKeyStorePath(sslKeyStorePath);
    contextFactory.setKeyStorePassword("secret");
    return contextFactory;
}        
```
        
That is a lot of Spring bean configuration, but it works! The server component references a special **sslConnector** bean
that defines the certificates and on the secure port **8443**. Client now have to use the certificate in order to connect.
       
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
