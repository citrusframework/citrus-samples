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

import java.util.Collections;

import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kafka.embedded.EmbeddedKafkaServer;
import org.citrusframework.kafka.embedded.EmbeddedKafkaServerBuilder;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Christoph Deppisch
 */
@Import(TodoAppAutoConfiguration.class)
@Configuration
public class EndpointConfig {

    private static final int KAFKA_BROKER_PORT = 9092;

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints
            .http()
                .client()
                .requestUrl("http://localhost:8080")
            .build();
    }

    @Bean
    public NamespaceContextBuilder namespaceContextBuilder() {
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("xh", "http://www.w3.org/1999/xhtml"));
        return namespaceContextBuilder;
    }

    @Bean
    public EmbeddedKafkaServer embeddedKafkaServer() {
        return new EmbeddedKafkaServerBuilder()
                .kafkaServerPort(KAFKA_BROKER_PORT)
                .topics("todo.inbound", "todo.report")
            .build();
    }

    @Bean
    public KafkaEndpoint todoKafkaEndpoint() {
        return CitrusEndpoints
            .kafka()
                .asynchronous()
                .server(String.format("localhost:%s", KAFKA_BROKER_PORT))
                .topic("todo.inbound")
            .build();
    }

    @Bean
    public KafkaEndpoint todoReportEndpoint() {
        return CitrusEndpoints
            .kafka()
                .asynchronous()
                .server(String.format("localhost:%s", KAFKA_BROKER_PORT))
                .topic("todo.report")
                .offsetReset("earliest")
            .build();
    }
}
