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
import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter;
import com.consol.citrus.endpoint.adapter.StaticEndpointAdapter;
import com.consol.citrus.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import com.consol.citrus.endpoint.adapter.mapping.SimpleMappingStrategy;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.message.Message;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

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
    public HttpServer httpServer(TestContextFactory contextFactory) {
        return new HttpServerBuilder()
                .port(8080)
                .autoStart(true)
                .endpointAdapter(staticResponseAdapter(contextFactory))
                .build();
    }

    @Bean
    public EndpointAdapter staticResponseAdapter(TestContextFactory contextFactory) {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put(HttpMethod.GET.name(), handleGetRequestAdapter(contextFactory));
        mappings.put(HttpMethod.POST.name(), handlePostRequestAdapter());
        mappings.put(HttpMethod.PUT.name(), handlePutRequestAdapter());
        mappings.put(HttpMethod.DELETE.name(), handleDeleteRequestAdapter());

        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();
        mappingStrategy.setAdapterMappings(mappings);
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy);

        dispatchingEndpointAdapter.setMappingKeyExtractor(new HeaderMappingKeyExtractor(HttpMessageHeaders.HTTP_REQUEST_METHOD));

        return dispatchingEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handlePostRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                String todoId = request.getHeader("X-TodoId").toString();
                return new HttpMessage(todoId).status(HttpStatus.CREATED);
            }
        };
    }

    @Bean
    public EndpointAdapter handlePutRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                String todoId = request.getHeader("X-TodoId").toString();
                return new HttpMessage(todoId)
                        .status(HttpStatus.OK);
            }
        };
    }

    @Bean
    public EndpointAdapter handleGetRequestAdapter(TestContextFactory contextFactory) {
        StaticEndpointAdapter responseEndpointAdapter = new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                String todoId = request.getHeader("X-TodoId").toString();
                return new HttpMessage("{\"id\": \"" + todoId + "\", \"title\": \"Sample task\", \"description\": \"Sample description\", \"done\": false}")
                        .header("X-TodoId", todoId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .status(HttpStatus.OK);
            }
        };

        responseEndpointAdapter.setTestContextFactory(contextFactory);
        return responseEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handleDeleteRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage().status(HttpStatus.NO_CONTENT);
            }
        };
    }
}
