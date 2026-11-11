SOAP SSL sample ![Logo][1]
==============

This sample uses SOAP web services in combination with SSL secure connectivity on both client and server. You can read more about the 
Citrus SOAP features in [reference guide][4]

Objectives
---------

In this sample project we want to configure both SOAP WebService client and server to use secure connections with SSL. First we need a 
keystore that holds the supported certificates. The sample uses the keystore in **src/test/resources/keys/citrus.jks**

We need a special Soap client configuration:

```java
@Bean
public WebServiceClient todoClient() {
    return CitrusEndpoints
        .soap()
            .client()
            .defaultUri(String.format("https://localhost:%s/services/ws/todolist", securePort))
            .messageSender(sslRequestMessageSender())
        .build();
}
```
    
The client component references a special request message sender and uses the transport scheme **https** on port **8443**. The SSL request message sender is defined in a
Java Spring configuration class simply because it is way more comfortable to do this in Java than in XML.
    
```java
@Bean
public HttpClient httpClient() {
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
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
            .build();
    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
        throw new BeanCreationException("Failed to create http client for ssl connection", e);
    }
}

@Bean
public HttpComponentsMessageSender sslRequestMessageSender() {
    return new HttpComponentsMessageSender(httpClient());
}
```
        
**Note**
We have to add the **HttpComponentsMessageSender.RemoveSoapHeadersInterceptor()** as interceptor to the http client. This prevents that content length headers get set several times which
is not allowed.

As you can see we load the keystore file **keys/citrus.jks** in order to setup the http client ssl context. In the Citrus test case you can use the client component as usual for 
sending messages to the server.

```java
$(soap()
    .client(todoClient)
    .send()
    .fork(true)
    .soapAction("addTodoEntry")
    .body(new ClassPathResource("templates/addTodoEntryRequest.xml")));

$(soap()
    .client(todoClient)
    .receive()
    .body(new ClassPathResource("templates/addTodoEntryResponse.xml")));    
```
        
On the server side the configuration looks like follows:
        
```java
@Bean
public WebServiceServer todoSslServer() {
    return CitrusEndpoints
        .soap()
            .server()
            .connector(sslConnector())
            .timeout(5000)
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
    
    SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
    secureRequestCustomizer.setSniHostCheck(false);
    configuration.setCustomizers(Collections.singletonList(secureRequestCustomizer));
    return configuration;
}

private SslContextFactory sslContextFactory() {
    SslContextFactory contextFactory = new SslContextFactory();
    contextFactory.setKeyStorePath(sslKeyStorePath);
    contextFactory.setKeyStorePassword("secret");
    return contextFactory;
}
```
       
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install
    
This executes the complete Maven build lifecycle. During the build you will see Citrus performing some integration tests.

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output. 
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
 [4]: https://citrusframework.org/reference/html#soap
