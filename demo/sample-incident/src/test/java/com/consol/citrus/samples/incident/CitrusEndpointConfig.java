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

package com.consol.citrus.samples.incident;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.jms.message.SoapJmsMessageConverter;
import org.citrusframework.report.MessageTracingTestListener;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderNotification;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderRequest;
import org.citrusframework.schema.samples.incidentmanager.v1.OpenIncident;
import org.citrusframework.schema.samples.incidentmanager.v1.OpenIncidentResponse;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncident;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncidentResponse;
import org.citrusframework.schema.samples.smsgateway.v1.SendSmsRequest;
import org.citrusframework.schema.samples.smsgateway.v1.SendSmsResponse;
import org.citrusframework.spi.Resources;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapAttachmentValidator;
import org.citrusframework.ws.validation.SimpleSoapFaultValidator;
import org.citrusframework.ws.validation.SoapAttachmentValidator;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.HandlerInterceptor;
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
    public SimpleXsdSchema soapEnvSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schemas/soap-envelope-1-1.xsd"));
    }

    @Bean
    public WsdlXsdSchema incidentManagerWsdl() {
        return new WsdlXsdSchema(Resources.create("schema/IncidentManager.wsdl", CitrusEndpointConfig.class));
    }

    @Bean
    public SimpleXsdSchema networkServiceXsd() {
        return new SimpleXsdSchema(new ClassPathResource("schema/NetworkService.xsd", CitrusEndpointConfig.class));
    }

    @Bean
    public SimpleXsdSchema fieldForceServiceXsd() {
        return new SimpleXsdSchema(new ClassPathResource("schema/FieldForceService.xsd", CitrusEndpointConfig.class));
    }

    @Bean
    public WsdlXsdSchema smsGatewayWsdl() {
        return new WsdlXsdSchema(Resources.create("schema/SmsGateway.wsdl", CitrusEndpointConfig.class));
    }

    @Bean
    public XsdSchemaRepository schemaRepository() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getSchemas().add(soapEnvSchema());
        schemaRepository.getSchemas().add(incidentManagerWsdl());
        schemaRepository.getSchemas().add(networkServiceXsd());
        schemaRepository.getSchemas().add(fieldForceServiceXsd());
        schemaRepository.getSchemas().add(smsGatewayWsdl());
        return schemaRepository;
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus IncidentManager sample");
        return variables;
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        Map<String, String> mappings = new HashMap<>();
        mappings.put("im", "http://www.citrusframework.org/schema/samples/IncidentManager/v1");
        mappings.put("net", "http://www.citrusframework.org/schema/samples/NetworkService/v1");
        mappings.put("ffs", "http://www.citrusframework.org/schema/samples/FieldForce/v1");
        mappings.put("sms", "http://www.citrusframework.org/schema/samples/SmsGateway/v1");
        mappings.put("sms", "http://www.citrusframework.org/schema/samples/SmsGateway/v1");
        namespaceContextBuilder.setNamespaceMappings(mappings);
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
    public SoapMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public SoapJmsMessageConverter soapJmsMessageConverter() {
        return new SoapJmsMessageConverter();
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
    public WebServiceClient incidentHttpClient() {
        return CitrusEndpoints.soap()
                .client()
                .defaultUri("http://localhost:18001/incident/IncidentManager/v1")
                .interceptors(clientInterceptors())
                .build();
    }

    @Bean
    public JmsSyncEndpoint incidentJmsEndpoint() {
        return CitrusEndpoints.jms()
                .synchronous()
                .connectionFactory(connectionFactory())
                .destination("JMS.Citrus.v1.IncidentManager")
                .messageConverter(soapJmsMessageConverter())
                .build();
    }

    @Bean
    public List<HandlerInterceptor> serverInterceptors() {
        return Collections.singletonList(new LoggingHandlerInterceptor());
    }

    @Bean
    public HttpServer networkBackendHttpServer() {
        return CitrusEndpoints.http()
                .server()
                .timeout(10000)
                .autoStart(true)
                .port(18002)
                .interceptors(serverInterceptors())
                .build();
    }

    @Bean
    public JmsEndpoint fieldForceOrderEndpoint() {
        return CitrusEndpoints.jms()
                .asynchronous()
                .connectionFactory(connectionFactory())
                .destination("JMS.Citrus.v1.FieldForceOrder")
                .build();
    }

    @Bean
    public JmsEndpoint fieldForceNotificationEndpoint() {
        return CitrusEndpoints.jms()
                .asynchronous()
                .connectionFactory(connectionFactory())
                .destination("JMS.Citrus.v1.FieldForceNotification")
                .build();
    }

    @Bean
    public WebServiceServer smsGatewayServer() {
        return CitrusEndpoints.soap()
                .server()
                .autoStart(true)
                .port(18005)
                .timeout(10000)
                .build();
    }

    @Bean
    public Marshaller jax2bMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(OpenIncident.class,
                                            OpenIncidentResponse.class,
                                            AnalyseIncident.class,
                                            AnalyseIncidentResponse.class,
                                            OrderRequest.class,
                                            OrderNotification.class,
                                            SendSmsRequest.class,
                                            SendSmsResponse.class);
        return jaxb2Marshaller;
    }
}
