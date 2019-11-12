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

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.interceptor.LoggingHandlerInterceptor;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.jms.message.SoapJmsMessageConverter;
import com.consol.citrus.report.MessageTracingTestListener;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.interceptor.LoggingClientInterceptor;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.ws.validation.*;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import com.consol.citrus.xml.schema.WsdlXsdSchema;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderNotification;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderRequest;
import org.citrusframework.schema.samples.incidentmanager.v1.OpenIncident;
import org.citrusframework.schema.samples.incidentmanager.v1.OpenIncidentResponse;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncident;
import org.citrusframework.schema.samples.networkservice.v1.AnalyseIncidentResponse;
import org.citrusframework.schema.samples.smsgateway.v1.SendSmsRequest;
import org.citrusframework.schema.samples.smsgateway.v1.SendSmsResponse;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;

import javax.jms.ConnectionFactory;
import java.util.*;

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
        return new WsdlXsdSchema(new ClassPathResource("schema/IncidentManager.wsdl", CitrusEndpointConfig.class));
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
        return new WsdlXsdSchema(new ClassPathResource("schema/SmsGateway.wsdl", CitrusEndpointConfig.class));
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

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory("tcp://localhost:61616");
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
