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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Configuration
@ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbc")
@EnableConfigurationProperties(JdbcConfigurationProperties.class)
public class JdbcApplicationConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "server", havingValue = "enabled", matchIfMissing = true)
    public Server database(JdbcConfigurationProperties configurationProperties) {
        Server database = new Server();
        try {
            HsqlProperties properties = new HsqlProperties();
            properties.setProperty("server.port", configurationProperties.getPort());
            properties.setProperty("server.database.0", configurationProperties.getFile());
            properties.setProperty("server.dbname.0", "testdb");
            properties.setProperty("server.remote_open", true);
            properties.setProperty("hsqldb.reconfig_logging", false);

            database.setProperties(properties);
        } catch (IOException | ServerAcl.AclFormatException e) {
            throw new BeanCreationException("Failed to create embedded database storage", e);
        }
        return database;
    }

    @Bean(name = "database")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "server", havingValue = "disabled")
    public String databaseDisabled() {
        return "todo.persistence.server.disabled";
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "transactional", havingValue = "false", matchIfMissing = true)
    public TodoListDao todoListJdbcDao() {
        return new JdbcTodoListDao();
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "transactional")
    public TodoListDao transactionalTodoListJdbcDao() {
        return new JdbcTransactionalTodoListDao();
    }

    @Bean(destroyMethod = "close")
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
