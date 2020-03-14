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

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter;
import com.consol.citrus.endpoint.adapter.StaticResponseEndpointAdapter;
import com.consol.citrus.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import com.consol.citrus.endpoint.adapter.mapping.SimpleMappingStrategy;
import com.consol.citrus.endpoint.adapter.mapping.SoapActionMappingKeyExtractor;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

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
    public SoapMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public WebServiceClient todoClient() {
        return CitrusEndpoints
            .soap()
                .client()
                .defaultUri("http://localhost:8080/services/ws/todolist")
            .build();
    }

    @Bean
    public WebServiceServer todoListServer(TestContextFactory contextFactory) {
        return CitrusEndpoints
            .soap()
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
        return new SoapActionMappingKeyExtractor();
    }

    @Bean
    public SimpleMappingStrategy mappingStrategy(TestContextFactory contextFactory) {
        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put("getTodo", todoResponseAdapter(contextFactory));
        mappings.put("getTodoList", todoListResponseAdapter(contextFactory));

        mappingStrategy.setAdapterMappings(mappings);
        return mappingStrategy;
    }

    @Bean
    public EndpointAdapter todoResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("<getTodoResponse xmlns=\"http://citrusframework.org/samples/todolist\">" +
                    "<todoEntry xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<id>${todoId}</id>" +
                        "<title>${todoName}</title>" +
                        "<description>${todoDescription}</description>" +
                        "<done>false</done>" +
                    "</todoEntry>" +
                "</getTodoResponse>");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }

    @Bean
    public EndpointAdapter todoListResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("<getTodoListResponse xmlns=\"http://citrusframework.org/samples/todolist\">" +
                    "<list>" +
                        "<todoEntry>" +
                            "<id>${todoId}</id>" +
                            "<title>${todoName}</title>" +
                            "<description>${todoDescription}</description>" +
                            "<done>false</done>" +
                        "</todoEntry>" +
                    "</list>" +
                "</getTodoListResponse>");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }

}
