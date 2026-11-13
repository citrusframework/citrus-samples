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

package org.citrusframework.samples.gettingstarted;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = { EndpointConfig.class })
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testTodoList() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Code some Citrus tests!");

        $(http().client(todoClient)
                .send()
                .post("/api/todolist")
                .message()
                .type(MessageType.JSON)
                .contentType("application/json")
                .body("{ \"id\": \"${todoId}\", " +
                        "\"title\": \"${todoName}\", " +
                        "\"description\": \"${todoDescription}\", " +
                        "\"done\": false}"));

        $(http().client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("${todoId}"));

        $(http().client(todoClient)
                .send()
                .get("/api/todo/${todoId}")
                .message()
                .accept("application/json"));

        $(http().client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .type(MessageType.JSON)
                .validate(validation().jsonPath()
                        .expression("$.id", "${todoId}")
                        .expression("$.title", "${todoName}")
                        .expression("$.description", "${todoDescription}")
                        .expression("$.done", false)));
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML,
            sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.xml")
    public void testTodoListXml() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML,
            sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.yaml")
    public void testTodoListYaml() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY,
            sources = "classpath:org/citrusframework/samples/gettingstarted/todo-list.citrus.groovy")
    public void testTodoListGroovy() {
    }
}
