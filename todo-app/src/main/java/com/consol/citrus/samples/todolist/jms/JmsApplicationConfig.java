/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.samples.todolist.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.*;

import javax.jms.ConnectionFactory;

/**
 * @author Christoph Deppisch
 */
@Configuration
@EnableJms
@ConditionalOnProperty(prefix = "todo.jms", value = "enabled")
public class JmsApplicationConfig {

    private String brokerHost = "localhost";

    private String brokerPort = "61616";

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "todo.jms", value = "broker", havingValue = "enabled", matchIfMissing = true)
    public BrokerService messageBroker() {
        try {
            BrokerService messageBroker = BrokerFactory.createBroker(String.format("broker:tcp://%s:%s", brokerHost, brokerPort));
            messageBroker.setPersistent(false);
            messageBroker.setUseJmx(false);
            return messageBroker;
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create embedded message broker", e);
        }
    }

    @Bean(name = "messageBroker")
    @ConditionalOnProperty(prefix = "todo.jms", value = "broker", havingValue = "disabled")
    public String messageBrokerDisabled() {
        return "todo.jms.broker.disabled";
    }

    @Bean
    @DependsOn("messageBroker")
    public ConnectionFactory activeMqConnectionFactory() {
        return new ActiveMQConnectionFactory(String.format("tcp://%s:%s", brokerHost, brokerPort));
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(activeMqConnectionFactory());
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTemplate;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setPubSubDomain(false);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
