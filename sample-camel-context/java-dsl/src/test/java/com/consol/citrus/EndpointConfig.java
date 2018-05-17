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

package com.consol.citrus;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.xsd.SimpleXsdSchema;

import javax.jms.ConnectionFactory;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
@Configuration
@PropertySource("citrus.properties")
public class EndpointConfig {

    @Bean
    public SimpleXsdSchema newsFeedXsd() {
        return new SimpleXsdSchema(new ClassPathResource("schemas/news.xsd"));
    }

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getSchemas().add(newsFeedXsd());
        return schemaRepository;
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus Integration Tests");
        return variables;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("nf", "http://citrusframework.org/schemas/samples/news"));
        return namespaceContextBuilder;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connectionFactory.setWatchTopicAdvisories(false);
        return connectionFactory;
    }

    @Bean
    public JmsEndpoint newsJmsEndpoint() {
        return CitrusEndpoints.jms()
                .asynchronous()
                .timeout(5000)
                .destination("JMS.Queue.News")
                .connectionFactory(connectionFactory())
                .build();
    }

    @Bean
    public WebServiceServer newsServer() {
        return CitrusEndpoints.soap()
                .server()
                .autoStart(true)
                .timeout(10000)
                .port(18009)
                .build();
    }

    @Bean
    public JmsComponent jms() {
        JmsComponent component = new JmsComponent();
        component.setConnectionFactory(connectionFactory());
        return component;
    }

    @Bean
    public CamelContext camelContext() throws Exception {
        SpringCamelContext context = new SpringCamelContext();
        context.addRouteDefinition(new RouteDefinition().from("jms:queue:JMS.Queue.News")
                                                    .to("log:com.consol.citrus.camel?level=INFO")
                                                    .to("spring-ws:http://localhost:18009?soapAction=newsFeed"));
        return context;
    }
}
