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

import java.util.Collections;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.xsd.SimpleXsdSchema;

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

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQ messageBroker() {
        EmbeddedActiveMQ broker = new EmbeddedActiveMQ();
        broker.setSecurityManager(securityManager());
        return broker;
    }

    @Bean
    public ActiveMQSecurityManager securityManager() {
        SecurityConfiguration securityConfiguration = new SecurityConfiguration(Collections.singletonMap("citrus", "citrus"),
                Collections.singletonMap("citrus", Collections.singletonList("citrus")));
        securityConfiguration.setDefaultUser("citrus");
        return new ActiveMQJAASSecurityManager(InVMLoginModule.class.getName(), securityConfiguration);
    }

    @Bean
    @DependsOn("messageBroker")
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory("tcp://localhost:61616", "citrus", "citrus");
    }

    @Bean
    public JmsEndpoint newsJmsEndpoint() {
        return CitrusEndpoints
            .jms()
                .asynchronous()
                .timeout(5000)
                .destination("JMS.Queue.News")
                .connectionFactory(connectionFactory())
            .build();
    }

    @Bean
    public WebServiceServer newsServer() {
        return CitrusEndpoints
            .soap()
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

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("connectionFactory")
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {
        SpringCamelContext context = new SpringCamelContext(applicationContext);
        context.addRouteDefinition(new RouteDefinition()
            .from("jms:queue:JMS.Queue.News")
            .to("log:org.citrusframework.camel?level=INFO")
            .to("spring-ws:http://localhost:18009?soapAction=newsFeed"));
        return context;
    }
}
