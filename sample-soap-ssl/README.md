SOAP SSL sample ![Logo][1]
==============

This sample uses SOAP web services in combination with SSL secure connectivity on both client and server. You can read more about the 
Citrus SOAP features in [reference guide][4]

Objectives
---------

In this sample project we want to configure both SOAP WebService client and server to use secure connections with SSL. First we need a 
keystore that holds the supported certificates. The sample uses the keystore in **src/test/resources/keys/citrus.jks**

We need a special Soap client configuration:

    <bean class="com.consol.citrus.samples.todolist.config.SoapClientSslConfig"/>

    <citrus-ws:client id="todoClient"
                        message-sender="sslRequestMessageSender"
                        request-url="https://localhost:8443"/>
    
The client component references a special request message sender and uses the transport scheme **https** on port **8443**. The SSL request message sender is defined in a
Java Spring configuration class simply because it is way more comfortable to do this in Java than in XML.
    
    @Configuration
    public class SoapClientSslConfig {
    
        @Bean
        public HttpClient httpClient() {
            try {
                SSLContext sslcontext = SSLContexts.custom()
                        .loadTrustMaterial(new ClassPathResource("keys/citrus.jks").getFile(), "secret".toCharArray(),
                                new TrustSelfSignedStrategy())
                        .build();
    
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                        sslcontext, NoopHostnameVerifier.INSTANCE);
    
                return HttpClients.custom()
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
    }
        
**Note**
We have to add the **HttpComponentsMessageSender.RemoveSoapHeadersInterceptor()** as interceptor to the http client. This prevents that content length headers get set several times which
is not allowed.

As you can see we load the keystore file **keys/citrus.jks** in order to setup the http client ssl context. In the Citrus test case you can use the client component as usual for 
sending messages to the server.

    soap()
        .client(todoClient)
        .send()
        .soapAction("addTodoEntry")
        .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));

    soap()
        .client(todoClient)
        .receive()
        .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));    
        
On the server side the configuration looks like follows:
        
    <citrus-ws:server id="todoSslServer"
                        connector="sslConnector"
                        auto-start="true"
                        timeout="5000"/>

    <bean id="sslConnector" class="org.eclipse.jetty.server.ServerConnector">
      <constructor-arg>
        <bean class="org.eclipse.jetty.server.Server"></bean>
      </constructor-arg>
      <constructor-arg>
        <list>
          <bean class="org.eclipse.jetty.server.SslConnectionFactory">
            <constructor-arg>
              <bean class="org.eclipse.jetty.util.ssl.SslContextFactory">
                <property name="keyStorePath" value="${project.basedir}/src/test/resources/keys/citrus.jks"/>
                <property name="keyStorePassword" value="secret"/>
              </bean>
            </constructor-arg>
            <constructor-arg value="http/1.1"/>
          </bean>
          <bean class="org.eclipse.jetty.server.HttpConnectionFactory">
            <constructor-arg>
              <bean class="org.eclipse.jetty.server.HttpConfiguration">
                <constructor-arg>
                  <bean class="org.eclipse.jetty.server.HttpConfiguration">
                    <property name="secureScheme" value="https"/>
                    <property name="securePort" value="8443"/>
                  </bean>
                </constructor-arg>
                <property name="customizers">
                  <list>
                    <bean class="org.eclipse.jetty.server.SecureRequestCustomizer"/>
                  </list>
                </property>
              </bean>
            </constructor-arg>
          </bean>
        </list>
      </constructor-arg>
      <property name="port" value="8443" />
    </bean>        
        
That is a lot of Spring bean configuration, but it works! The server component references a special **sslConnector** bean
that defines the certificates and on the secure port **8443**. Client now have to use the certificate in order to connect.
       
In the test case we can receive the requests and provide proper response messages as usual.

    soap()
        .server(todoServer)
        .receive()
        .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));

    soap()
        .server(todoServer)
        .send()
        .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));
       
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install
    
This executes the complete Maven build lifecycle. During the build you will see Citrus performing some integration tests.

Execute all Citrus tests by calling

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output. 
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
 [4]: http://www.citrusframework.org/reference/html/soap.html