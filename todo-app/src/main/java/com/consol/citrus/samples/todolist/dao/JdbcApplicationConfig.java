/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.samples.todolist.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Configuration
@EnableConfigurationProperties(JdbcConfigurationProperties.class)
public class JdbcApplicationConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "server", havingValue = "enabled", matchIfMissing = true)
    public Server database() {
        Server database = new Server();
        try {
            HsqlProperties properties = new HsqlProperties();
            properties.setProperty("server.port", "9099");
            properties.setProperty("server.database.0", "file:target/testdb");
            properties.setProperty("server.dbname.0", "testdb");
            properties.setProperty("server.remote_open", true);
            properties.setProperty("hsqldb.reconfig_logging", false);

            database.setProperties(properties);
        } catch (IOException | ServerAcl.AclFormatException e) {
            throw new BeanCreationException("Failed to create embedded database storage", e);
        }
        return database;
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "in_memory", matchIfMissing = true)
    public TodoListDao todoListInMemoryDao() {
        return new InMemoryTodoListDao();
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbc")
    public TodoListDao todoListJdbcDao(Environment environment) {
        PropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "todo.persistence.");
        if (!propertyResolver.getProperty("transactional", "false").equals("false")) {
            return new JdbcTransactionalTodoListDao();
        } else {
            return new JdbcTodoListDao();
        }
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbc")
    @DependsOn("database")
    public BasicDataSource dataSource(JdbcConfigurationProperties configurationProperties) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(configurationProperties.getDriverClassName());
        dataSource.setUrl(configurationProperties.getUrl());
        dataSource.setUsername(configurationProperties.getUsername());
        dataSource.setPassword(configurationProperties.getPassword());

        return dataSource;
    }
}
