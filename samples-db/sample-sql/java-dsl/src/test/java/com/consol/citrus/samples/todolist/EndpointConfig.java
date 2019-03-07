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

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerAfterSuiteSupport;
import com.consol.citrus.dsl.runner.TestRunnerBeforeSuiteSupport;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

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
    public SequenceBeforeSuite beforeSuite() {
        return new TestRunnerBeforeSuiteSupport() {
            @Override
            public void beforeSuite(TestRunner runner) {
                runner.sql(executeSQLBuilder -> executeSQLBuilder
                    .dataSource(todoListDataSource())
                    .statement("CREATE TABLE IF NOT EXISTS todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)"));
            }
        };
    }

    @Bean
    public SequenceAfterSuite afterSuite() {
        return new TestRunnerAfterSuiteSupport() {
            @Override
            public void afterSuite(TestRunner runner) {
                runner.sql(executeSQLBuilder -> executeSQLBuilder
                    .dataSource(todoListDataSource())
                    .statement("DELETE FROM todo_entries"));
            }
        };
    }

    @Bean(destroyMethod = "close")
    public BasicDataSource todoListDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:hsql://localhost:9099/testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setInitialSize(1);
        dataSource.setMaxActive(5);
        dataSource.setMaxIdle(2);
        return dataSource;
    }
}
