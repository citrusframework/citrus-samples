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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.interceptor.LoggingEndpointInterceptor;
import org.citrusframework.ws.interceptor.SoapMustUnderstandEndpointInterceptor;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.xml.xsd.SimpleXsdSchema;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

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
                .defaultUri("http://localhost:8080/services/ws/todolist")
                .interceptors(clientInterceptors())
            .build();
    }

    @Bean
    public List<ClientInterceptor> clientInterceptors() {
        return Arrays.asList(wss4jSecurityClientInterceptor(), new LoggingClientInterceptor());
    }

    @Bean
    public Wss4jSecurityInterceptor wss4jSecurityClientInterceptor() {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

        interceptor.setSecurementActions("Timestamp UsernameToken");
        interceptor.setSecurementUsername("admin");
        interceptor.setSecurementPassword("secret");

        return interceptor;
    }

    @Bean
    public WebServiceServer todoListServer() {
        return CitrusEndpoints
            .soap()
                .server()
                .autoStart(true)
                .port(8080)
                .interceptors(serverInterceptors())
            .build();
    }

    @Bean
    public List<EndpointInterceptor> serverInterceptors() {
        return Arrays.asList(soapMustUnderstandEndpointInterceptor(), wss4jSecurityServerInterceptor(), new LoggingEndpointInterceptor());
    }

    @Bean
    public EndpointInterceptor soapMustUnderstandEndpointInterceptor() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        interceptor.setAcceptedHeaders(Collections.singletonList("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}Security"));
        return interceptor;
    }

    @Bean
    public Wss4jSecurityInterceptor wss4jSecurityServerInterceptor() {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

        interceptor.setValidationActions("Timestamp UsernameToken");

        SimplePasswordValidationCallbackHandler validationCallbackHandler = new SimplePasswordValidationCallbackHandler();
        validationCallbackHandler.setUsersMap(Collections.singletonMap("admin", "secret"));
        interceptor.setValidationCallbackHandler(validationCallbackHandler);

        return interceptor;
    }

}
