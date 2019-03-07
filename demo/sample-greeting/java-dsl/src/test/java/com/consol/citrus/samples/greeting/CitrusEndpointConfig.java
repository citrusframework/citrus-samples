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

import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeTestSupport;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
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
@ImportResource("classpath:com/consol/citrus/samples/greeting/channel/greeting-channel.xml")
public class CitrusEndpointConfig {

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

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(jmsBrokerUrl);
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
    public SequenceBeforeTest beforeTest() {
        return new TestRunnerBeforeTestSupport() {
            @Override
            public void beforeTest(TestRunner testRunner) {
                testRunner.purgeQueues(purgeJmsQueueBuilder -> purgeJmsQueueBuilder
                    .connectionFactory(connectionFactory())
                    .queue(greetingRequestQueue)
                    .queue(greetingResponseQueue));
            }
        };
    }
}
