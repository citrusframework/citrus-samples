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

package com.consol.citrus.samples.greeting;

import java.util.Collections;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.citrusframework.DefaultTestActions;
import org.citrusframework.TestActions;
import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.xsd.SimpleXsdSchema;

/**
 * @author Christoph Deppisch
 */
@Configuration
@PropertySource("citrus.properties")
@ImportResource("classpath:com/consol/citrus/samples/greeting/channel/greeting-channel.xml")
public class CitrusEndpointConfig {

    private final TestActions actions = new DefaultTestActions();

    @Value("${jms.broker.url}")
    private String jmsBrokerUrl;

    @Value("${greeting.request.queue}")
    private String greetingRequestQueue;

    @Value("${greeting.response.queue}")
    private String greetingResponseQueue;

    @Bean
    public SimpleXsdSchema greetingSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/greeting.xsd", CitrusEndpointConfig.class));
    }

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getSchemas().add(greetingSchema());
        return schemaRepository;
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus Greeting sample");
        return variables;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("bkr", "http://www.consol.com/schemas/bookstore"));
        return namespaceContextBuilder;
    }

    @Bean
    public ChannelEndpoint greetingsEndpoint() {
        return CitrusEndpoints.channel()
            .asynchronous()
            .channel("greetings")
            .timeout(5000L)
            .build();
    }

    @Bean
    public ChannelEndpoint greetingsTransformedEndpoint() {
        return CitrusEndpoints.channel()
            .asynchronous()
            .channel("greetingsTransformed")
            .timeout(5000L)
            .build();
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
        return new ActiveMQConnectionFactory(jmsBrokerUrl, "citrus", "citrus");
    }

    @Bean
    public JmsEndpoint greetingJmsRequestSender() {
        return CitrusEndpoints.jms()
            .asynchronous()
            .connectionFactory(connectionFactory())
            .destination(greetingRequestQueue)
            .build();
    }

    @Bean
    public JmsEndpoint greetingJmsResponseReceiver() {
        return CitrusEndpoints.jms()
            .asynchronous()
            .connectionFactory(connectionFactory())
            .destination(greetingResponseQueue)
            .build();
    }

    @Bean
    public BeforeTest beforeTest(ConnectionFactory connectionFactory) {
        return new SequenceBeforeTest.Builder()
            .actions(actions.purgeQueues()
                    .connectionFactory(connectionFactory)
                    .queue(greetingRequestQueue)
                    .queue(greetingResponseQueue)
            )
            .build();
    }
}
