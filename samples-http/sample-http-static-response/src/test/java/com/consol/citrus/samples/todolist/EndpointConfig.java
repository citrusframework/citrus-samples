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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import org.citrusframework.endpoint.adapter.mapping.SimpleMappingStrategy;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.variable.GlobalVariables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("todoId", "702c4a4e-5c8a-4ce2-a451-4ed435d3604a");
        variables.getVariables().put("todoName", "todo_1871");
        variables.getVariables().put("todoDescription", "Description: todo_1871");
        return variables;
    }

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints
            .http()
                .client()
                .requestUrl("http://localhost:8080")
            .build();
    }

    @Bean
    public HttpServer todoListServer(TestContextFactory contextFactory) throws Exception {
        return CitrusEndpoints
            .http()
                .server()
                .port(8080)
                .endpointAdapter(dispatchingEndpointAdapter(contextFactory))
                .timeout(10000)
                .autoStart(true)
            .build();
    }

    @Bean
    public RequestDispatchingEndpointAdapter dispatchingEndpointAdapter(TestContextFactory contextFactory) {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();
        dispatchingEndpointAdapter.setMappingKeyExtractor(mappingKeyExtractor());
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy(contextFactory));
        return dispatchingEndpointAdapter;
    }

    @Bean
    public HeaderMappingKeyExtractor mappingKeyExtractor() {
        HeaderMappingKeyExtractor mappingKeyExtractor = new HeaderMappingKeyExtractor();
        mappingKeyExtractor.setHeaderName(HttpMessageHeaders.HTTP_REQUEST_URI);
        return mappingKeyExtractor;
    }

    @Bean
    public SimpleMappingStrategy mappingStrategy(TestContextFactory contextFactory) {
        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put("/api/todo", todoResponseAdapter(contextFactory));
        mappings.put("/api/todolist", todoListResponseAdapter(contextFactory));

        mappingStrategy.setAdapterMappings(mappings);
        return mappingStrategy;
    }

    @Bean
    public EndpointAdapter todoResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("{" +
                            "\"id\": \"${todoId}\"," +
                            "\"title\": \"${todoName}\"," +
                            "\"description\": \"${todoDescription}\"," +
                            "\"done\": false" +
                        "}");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }

    @Bean
    public EndpointAdapter todoListResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("[" +
                            "{" +
                                "\"id\": \"${todoId}\"," +
                                "\"title\": \"${todoName}\"," +
                                "\"description\": \"${todoDescription}\"," +
                                "\"done\": false" +
                            "}" +
                        "]");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }
}
