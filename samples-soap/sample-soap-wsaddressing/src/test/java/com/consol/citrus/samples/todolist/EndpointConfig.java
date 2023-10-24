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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.ws.addressing.WsAddressingHeaders;
import org.citrusframework.ws.addressing.WsAddressingVersion;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingEndpointInterceptor;
import org.citrusframework.ws.interceptor.SoapMustUnderstandEndpointInterceptor;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
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
                .messageConverter(wsAddressingMessageConverter())
            .build();
    }

    @Bean
    public WebServiceMessageConverter wsAddressingMessageConverter() {
        WsAddressingHeaders addressingHeaders = new WsAddressingHeaders();

        addressingHeaders.setVersion(WsAddressingVersion.VERSION200408);
        addressingHeaders.setAction(URI.create("http://citrusframework.org/samples/todolist"));
        addressingHeaders.setTo(URI.create("http://citrusframework.org/samples/todolist"));

        addressingHeaders.setFrom(new EndpointReference(URI.create("http://citrusframework.org/samples/client")));
        addressingHeaders.setReplyTo(new EndpointReference(URI.create("http://citrusframework.org/samples/client")));
        addressingHeaders.setFaultTo(new EndpointReference(URI.create("http://citrusframework.org/samples/client/fault")));

        return new WsAddressingMessageConverter(addressingHeaders);
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
        return Arrays.asList(soapMustUnderstandEndpointInterceptor(), new LoggingEndpointInterceptor());
    }

    @Bean
    public EndpointInterceptor soapMustUnderstandEndpointInterceptor() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        interceptor.setAcceptedHeaders(Collections.singletonList("{http://schemas.xmlsoap.org/ws/2004/08/addressing}To"));
        return interceptor;
    }
}
