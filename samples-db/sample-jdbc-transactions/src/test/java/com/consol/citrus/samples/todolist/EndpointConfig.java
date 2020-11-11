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

import com.consol.citrus.db.driver.JdbcDriver;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * @author Christoph Deppisch
 */
@Import(TodoAppAutoConfiguration.class)
@Configuration
public class EndpointConfig {

    private static final int DB_SERVER_PORT = 13306;

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
    public JdbcServer jdbcServer() {
        return CitrusEndpoints
            .jdbc()
                .server()
                .host("localhost")
                .databaseName("testdb")
                .port(DB_SERVER_PORT)
                .timeout(2000L)
                .autoStart(true)
                .autoTransactionHandling(false)
            .build();
    }

    @Bean
    public SingleConnectionDataSource dataSource() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName(JdbcDriver.class.getName());
        dataSource.setUrl(String.format("jdbc:citrus:http://localhost:%s/testdb", DB_SERVER_PORT));
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
