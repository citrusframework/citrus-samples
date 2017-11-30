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
import com.consol.citrus.endpoint.adapter.StaticEndpointAdapter;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.message.Message;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

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
    public HttpClient todoClient() {
        return CitrusEndpoints.http()
                            .client()
                            .requestUrl("https://localhost:" + securePort)
                            .requestFactory(sslRequestFactory())
                            .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public org.apache.http.client.HttpClient httpClient() {
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
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }

    @Bean
    public HttpServer todoSslServer() throws Exception {
        return CitrusEndpoints.http()
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

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("xh", "http://www.w3.org/1999/xhtml"));
        return namespaceContextBuilder;
    }
}
