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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testJsonPayloadValidation() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "Sample task");
        variable("todoDescription", "Sample description");
        variable("done", "false");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .header("X-TodoId", "${todoId}")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.CREATED)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .header("X-TodoId", "${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @Test
    @CitrusTest
    public void testJsonValidationWithFileResource() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "Sample task");
        variable("todoDescription", "Sample description");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .header("X-TodoId", "${todoId}")
            .payload(new ClassPathResource("templates/todo.json")));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.CREATED)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .header("X-TodoId", "${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload(new ClassPathResource("templates/todo.json")));
    }

    @Test
    @CitrusTest
    public void testJsonPathValidation() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "Sample task");
        variable("todoDescription", "Sample description");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .header("X-TodoId", "${todoId}")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.CREATED)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .header("X-TodoId", "${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .validate("$.id", "${todoId}")
            .validate("$.title", "${todoName}")
            .validate("$.description", "${todoDescription}")
            .validate("$.done", false));
    }

}
