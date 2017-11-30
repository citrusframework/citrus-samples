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

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter;
import com.consol.citrus.endpoint.adapter.StaticResponseEndpointAdapter;
import com.consol.citrus.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import com.consol.citrus.endpoint.adapter.mapping.SimpleMappingStrategy;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.variable.GlobalVariables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
        return CitrusEndpoints.http()
                            .client()
                            .requestUrl("http://localhost:8080")
                            .build();
    }

    @Bean
    public HttpServer todoListServer() throws Exception {
        return CitrusEndpoints.http()
                .server()
                .port(8080)
                .endpointAdapter(dispatchingEndpointAdapter())
                .timeout(10000)
                .autoStart(true)
                .build();
    }

    @Bean
    public RequestDispatchingEndpointAdapter dispatchingEndpointAdapter() {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();
        dispatchingEndpointAdapter.setMappingKeyExtractor(mappingKeyExtractor());
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy());
        return dispatchingEndpointAdapter;
    }

    @Bean
    public HeaderMappingKeyExtractor mappingKeyExtractor() {
        HeaderMappingKeyExtractor mappingKeyExtractor = new HeaderMappingKeyExtractor();
        mappingKeyExtractor.setHeaderName(HttpMessageHeaders.HTTP_REQUEST_URI);
        return mappingKeyExtractor;
    }

    @Bean
    public SimpleMappingStrategy mappingStrategy() {
        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put("/todo", todoResponseAdapter());
        mappings.put("/todolist", todoListResponseAdapter());

        mappingStrategy.setAdapterMappings(mappings);
        return mappingStrategy;
    }

    @Bean
    public EndpointAdapter todoResponseAdapter() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("{" +
                            "\"id\": \"${todoId}\"," +
                            "\"title\": \"${todoName}\"," +
                            "\"description\": \"${todoDescription}\"," +
                            "\"done\": false" +
                        "}");
        return endpointAdapter;
    }

    @Bean
    public EndpointAdapter todoListResponseAdapter() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("[" +
                            "{" +
                                "\"id\": \"${todoId}\"," +
                                "\"title\": \"${todoName}\"," +
                                "\"description\": \"${todoDescription}\"," +
                                "\"done\": false" +
                            "}" +
                        "]");
        return endpointAdapter;
    }
}
