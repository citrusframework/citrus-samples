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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public SimpleXsdSchema todoListSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/ImageService.xsd"));
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
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("image", "http://www.citrusframework.org/imageService"));
        return namespaceContextBuilder;
    }

    @Bean
    public SoapMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public WebServiceClient imageClient() {
        return CitrusEndpoints
            .soap()
                .client()
                .defaultUri("http://localhost:8080/services/image")
            .build();
    }

    @Bean
    public WebServiceServer imageServer() {
        return CitrusEndpoints
            .soap()
                .server()
                .port(8080)
                .autoStart(true)
            .build();
    }

}
