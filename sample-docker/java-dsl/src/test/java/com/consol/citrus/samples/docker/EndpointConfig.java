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

package com.consol.citrus.samples.docker;

import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Value("${todo.server.host:localhost}")
    private String todoServerHost;

    @Value("${todo.server.port:8080}")
    private String todoServerPort;

    @Bean
    public DockerClient dockerClient() {
        return CitrusEndpoints.docker()
                .client()
                .url("unix:///var/run/dockerhost/docker.sock")
                .build();
    }

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints.http()
                            .client()
                            .requestUrl(String.format("http://%s:%s", todoServerHost, todoServerPort))
                            .build();
    }
}
