/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.xml.xsd.SimpleXsdSchema;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
@Configuration
@PropertySource("citrus.properties")
public class EndpointConfig {

    @Value("${project.basedir}/src/test/resources/keys/citrus.jks")
    private String sslKeyStorePath;

    private final int securePort = 8443;

    @Bean
    public SimpleXsdSchema todoListSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/TodoList.xsd"));
    }

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getSchemas().add(todoListSchema());
        return schemaRepository;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("todo", "http://citrusframework.org/samples/todolist"));
        return namespaceContextBuilder;
    }

    @Bean
    public SoapMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public WebServiceClient todoClient() {
        return CitrusEndpoints
            .soap()
                .client()
                .defaultUri(String.format("https://localhost:%s/services/ws/todolist", securePort))
                .messageSender(sslRequestMessageSender())
            .build();
    }

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
        configuration.setCustomizers(Collections.singletonList(new SecureRequestCustomizer()));
        return configuration;
    }

    private SslContextFactory sslContextFactory() {
        SslContextFactory contextFactory = new SslContextFactory();
        contextFactory.setKeyStorePath(sslKeyStorePath);
        contextFactory.setKeyStorePassword("secret");
        return contextFactory;
    }

}
