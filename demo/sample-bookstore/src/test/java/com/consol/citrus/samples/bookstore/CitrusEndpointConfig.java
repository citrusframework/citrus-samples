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

package com.consol.citrus.samples.bookstore;

import java.util.Collections;
import java.util.List;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.report.MessageTracingTestListener;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.interceptor.LoggingClientInterceptor;
import com.consol.citrus.ws.validation.SimpleSoapAttachmentValidator;
import com.consol.citrus.ws.validation.SimpleSoapFaultValidator;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import com.consol.citrus.xml.Jaxb2Marshaller;
import com.consol.citrus.xml.Marshaller;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;

/**
 * @author Christoph Deppisch
 */
@Configuration
@PropertySource("citrus.properties")
public class CitrusEndpointConfig {

    @Bean
    public SimpleXsdSchema bookStoreSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/BookStoreSchema.xsd", CitrusEndpointConfig.class));
    }

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getSchemas().add(bookStoreSchema());
        return schemaRepository;
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus BookStore sample");
        return variables;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("bkr", "http://www.consol.com/schemas/bookstore"));
        return namespaceContextBuilder;
    }

    @Bean
    public MessageTracingTestListener messageTracingTestListener() {
        return new MessageTracingTestListener();
    }

    @Bean
    public List<ClientInterceptor> clientInterceptors() {
        return Collections.singletonList(new LoggingClientInterceptor());
    }

    @Bean
    public SoapMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public SoapFaultValidator soapFaultValidator() {
        return new SimpleSoapFaultValidator();
    }

    @Bean
    public SoapAttachmentValidator soapAttachmentValidator() {
        return new SimpleSoapAttachmentValidator();
    }

    @Bean
    public WebServiceClient bookStoreClient() {
        return CitrusEndpoints.soap()
                .client()
                .defaultUri("http://localhost:18001/bookstore")
                .interceptors(clientInterceptors())
                .build();
    }

    @Bean
    public Marshaller jax2bMarshaller() {
        return new Jaxb2Marshaller("com.consol.citrus.samples.bookstore.model");
    }
}
