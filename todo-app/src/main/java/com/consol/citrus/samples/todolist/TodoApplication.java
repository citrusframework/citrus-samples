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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Christoph Deppisch
 */
@SpringBootApplication(scanBasePackages = {
        "com.consol.citrus.samples.todolist.dao",
        "com.consol.citrus.samples.todolist.jms",
        "com.consol.citrus.samples.todolist.kafka",
        "com.consol.citrus.samples.todolist.service",
        "com.consol.citrus.samples.todolist.soap",
        "com.consol.citrus.samples.todolist.web"
})
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }

    @Bean
    public OpenAPI todoApi() {
        return new OpenAPI()
                .info(new Info().title("TodoList API")
                        .description("REST API for todo application")
                        .version("2.0")
                        .license(new License().name("Apache 2.0")));
    }
}
