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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.Message;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author Christoph Deppisch
 */
@Configuration
@PropertySource("classpath:citrus.properties")
public class EndpointConfig {

    @Value("${project.basedir}/src/test/resources/keys/citrus.jks")
    private String sslKeyStorePath;

    private final int securePort = 8443;

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints
            .http()
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
    public org.apache.hc.client5.http.classic.HttpClient httpClient() {
        try {
            SSLContext sslcontext = SSLContexts
                .custom()
                    .loadTrustMaterial(new ClassPathResource("keys/citrus.jks").getFile(), "secret".toCharArray(),
                            new TrustSelfSignedStrategy())
                .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext, NoopHostnameVerifier.INSTANCE);

            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }

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
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniHostCheck(false);
        configuration.setCustomizers(Collections.singletonList(secureRequestCustomizer));
        return configuration;
    }

    private SslContextFactory.Server sslContextFactory() {
        SslContextFactory.Server contextFactory = new SslContextFactory.Server();
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
