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

package com.consol.citrus.samples.bakery;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.report.MessageTracingTestListener;
import com.consol.citrus.variable.GlobalVariables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration
@PropertySource(value = "citrus.properties")
public class CitrusEndpointConfig {

    @Value("${mail.server.port}")
    public int mailServerPort;

    @Value("${report.server.port}")
    public int reportServerPort;

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables variables = new GlobalVariables();
        variables.getVariables().put("project.name", "Citrus Bakery sample");
        return variables;
    }

    @Bean
    public MessageTracingTestListener messageTracingTestListener() {
        return new MessageTracingTestListener();
    }

    @Bean
    public HttpClient reportingClient() {
        return CitrusEndpoints.http()
                    .client()
                    .requestMethod(HttpMethod.GET)
                    .requestUrl(String.format("http://localhost:%s/report/services", reportServerPort))
                    .build();
    }

    @Bean
    public MailServer mailServer() {
        return CitrusEndpoints.mail()
                .server()
                .port(mailServerPort)
                .autoAccept(true)
                .autoStart(true)
                .build();
    }
}
