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

package com.consol.citrus.samples.flightbooking;

import java.util.Collections;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.apache.commons.dbcp2.BasicDataSource;
import org.citrusframework.DefaultTestActions;
import org.citrusframework.TestActions;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration
@PropertySource(value = "citrus.properties")
public class CitrusEndpointConfig {

    private final TestActions actions = new DefaultTestActions();

    @Value("${jms.broker.url}")
    private String jmsBrokerUrl;

    @Value("${travel.agency.request.queue}")
    private String travelAgencyRequestQueue;

    @Value("${travel.agency.response.queue}")
    private String travelAgencyResponseQueue;

    @Value("${smart.airline.request.queue}")
    private String smartAirlineBookingRequestEndpoint;

    @Value("${smart.airline.response.queue}")
    private String smartAirlineBookingResponseEndpoint;

    @Value("${jdbc.driver}")
    private String jdbcDiver;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.password}")
    private String dbPassword;

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:com/consol/citrus/samples/flightbooking/schema/FlightBookingSchema.xsd");
        return schemaRepository;
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus FlightBooking sample");
        return variables;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("fbs", "http://www.consol.com/schemas/flightbooking"));
        return namespaceContextBuilder;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQ messageBroker(ActiveMQSecurityManager securityManager) {
        EmbeddedActiveMQ broker = new EmbeddedActiveMQ();
        broker.setSecurityManager(securityManager);
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
    public HttpServer royalAirlineServer() {
        return CitrusEndpoints.http()
            .server()
            .autoStart(true)
            .port(8074)
            .timeout(5000)
            .build();
    }

    @Bean
    public JmsEndpoint travelAgencyBookingRequestEndpoint(ConnectionFactory connectionFactory) {
        return CitrusEndpoints.jms()
            .asynchronous()
            .destination(travelAgencyRequestQueue)
            .connectionFactory(connectionFactory)
            .build();
    }

    @Bean
    public JmsEndpoint travelAgencyBookingResponseEndpoint(ConnectionFactory connectionFactory) {
        return CitrusEndpoints.jms()
            .asynchronous()
            .destination(travelAgencyResponseQueue)
            .connectionFactory(connectionFactory)
            .build();
    }

    @Bean
    public JmsEndpoint smartAirlineBookingRequestEndpoint(ConnectionFactory connectionFactory) {
        return CitrusEndpoints.jms()
            .asynchronous()
            .destination(smartAirlineBookingRequestEndpoint)
            .connectionFactory(connectionFactory)
            .build();
    }

    @Bean
    public JmsEndpoint smartAirlineBookingResponseEndpoint(ConnectionFactory connectionFactory) {
        return CitrusEndpoints.jms()
            .asynchronous()
            .destination(smartAirlineBookingResponseEndpoint)
            .connectionFactory(connectionFactory)
            .build();
    }

    @Bean(destroyMethod = "close")
    public BasicDataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(jdbcDiver);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setInitialSize(1);
        dataSource.setMaxTotal(5);
        dataSource.setMaxIdle(2);
        return dataSource;
    }

    @Bean
    public BeforeTest beforeTest(ConnectionFactory connectionFactory) {
        return new SequenceBeforeTest.Builder()
                .actions(actions.purgeQueues()
                    .connectionFactory(connectionFactory)
                    .queue(travelAgencyRequestQueue)
                    .queue(travelAgencyResponseQueue)
                    .queue(smartAirlineBookingRequestEndpoint)
                    .queue(smartAirlineBookingResponseEndpoint))
            .build();
    }
}
