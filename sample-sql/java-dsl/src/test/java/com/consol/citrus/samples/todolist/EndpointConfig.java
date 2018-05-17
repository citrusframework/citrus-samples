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
import com.consol.citrus.dsl.design.*;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints.http()
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
        return new TestDesignerBeforeSuiteSupport() {
            @Override
            public void beforeSuite(TestDesigner designer) {
                designer.sql(todoListDataSource())
                    .statement("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)");
            }
        };
    }

    @Bean
    public SequenceAfterSuite afterSuite() {
        return new TestDesignerAfterSuiteSupport() {
            @Override
            public void afterSuite(TestDesigner designer) {
                designer.sql(todoListDataSource())
                    .statement("DELETE FROM todo_entries");
            }
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server hsqldbServer() throws IOException, ServerAcl.AclFormatException {
        Server dbServer = new Server();

        HsqlProperties properties = new HsqlProperties();
        properties.setProperty("server.database.0", "file:target/testdb");
        properties.setProperty("server.dbname.0", "testdb");
        properties.setProperty("server.remote_open", true);
        properties.setProperty("hsqldb.reconfig_logging", false);

        dbServer.setProperties(properties);
        return dbServer;
    }

    @Bean(destroyMethod = "close")
    public BasicDataSource todoListDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:hsql://localhost/testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setInitialSize(1);
        dataSource.setMaxActive(5);
        dataSource.setMaxIdle(2);
        return dataSource;
    }
}
