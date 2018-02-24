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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.samples.todolist.dao.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Christoph Deppisch
 */
@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties(JdbcConfigurationProperties.class)
public class TodoApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(TodoApplication.class);
    }

    @Bean
    public Docket todoApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.regex("/api.*"))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "in_memory", matchIfMissing = true)
    public TodoListDao todoListInMemoryDao() {
        return new InMemoryTodoListDao();
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbc")
    public TodoListDao todoListJdbcDao() {
        return new JdbcTodoListDao();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbc")
    public BasicDataSource jdbcDataSource(final JdbcConfigurationProperties configurationProperties) {
        return getBasicDataSource(configurationProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbcTransactional")
    public TodoListDao transactionalTodoListJdbcDao() {
        return new JdbcTransactionToDoListDao();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "jdbcTransactional")
    public BasicDataSource transactionalJdbcDataSource(final JdbcConfigurationProperties configurationProperties) {
        return getBasicDataSource(configurationProperties);
    }

    private BasicDataSource getBasicDataSource(final JdbcConfigurationProperties configurationProperties) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(configurationProperties.getDriverClassName());
        dataSource.setUrl(configurationProperties.getUrl());
        dataSource.setUsername(configurationProperties.getUsername());
        dataSource.setPassword(configurationProperties.getPassword());

        return dataSource;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("TodoList API")
                .description("REST API for todo application")
                .license("Apache License Version 2.0")
                .version("2.0")
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}