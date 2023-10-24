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

import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.server.RmiServer;
import com.consol.citrus.samples.todolist.remote.TodoListService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public RmiClient rmiClient() {
        return CitrusEndpoints
            .rmi()
                .client()
                .serverUrl("rmi://localhost:1099/todoService")
            .build();
    }

    @Bean
    public RmiServer rmiServer() {
        return CitrusEndpoints
            .rmi()
                .server()
                .autoStart(true)
                .host("localhost")
                .port(1099)
                .remoteInterfaces(TodoListService.class)
                .binding("todoService")
                .createRegistry(true)
            .build();
    }
}
