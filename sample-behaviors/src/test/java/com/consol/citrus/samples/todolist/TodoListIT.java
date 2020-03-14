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

import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.AbstractTestBehavior;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.http.actions.HttpClientRequestActionBuilder;
import com.consol.citrus.http.actions.HttpClientResponseActionBuilder;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
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
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        applyBehavior(new AddTodoBehavior()
            .withPayloadData("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        applyBehavior(new GetTodoBehavior("${todoId}")
            .validate("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @Test
    @CitrusTest
    public void testJsonValidationWithFileResource() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        applyBehavior(new AddTodoBehavior()
                            .withResource(new ClassPathResource("templates/todo.json")));

        applyBehavior(new GetTodoBehavior("${todoId}")
                            .validate(new ClassPathResource("templates/todo.json")));
    }

    @Test
    @CitrusTest
    public void testJsonPathValidation() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        applyBehavior(new AddTodoBehavior()
            .withPayloadData("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"));

        applyBehavior(new GetTodoBehavior("${todoId}")
            .validate("$.id", "${todoId}")
            .validate("$.title", "${todoName}")
            .validate("$.description", "${todoDescription}")
            .validate("$.done", false));
    }

    /**
     * Adds new entry via Http POST request
     */
    private class AddTodoBehavior extends AbstractTestBehavior {

        private String payloadData;
        private Resource resource;

        @Override
        public void apply() {

            HttpClientRequestActionBuilder request = new HttpActionBuilder()
                .client(todoClient)
                .send()
                .post("/api/todolist")
                .messageType(MessageType.JSON)
                .contentType(ContentType.APPLICATION_JSON.getMimeType());

            if (StringUtils.hasText(payloadData)) {
                request.payload(payloadData);
            } else if (resource != null) {
                request.payload(resource);
            }
            run(request.build());

            http(httpActionBuilder -> httpActionBuilder
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("${todoId}"));
        }

        AddTodoBehavior withPayloadData(String payload) {
            this.payloadData = payload;
            return this;
        }

        AddTodoBehavior withResource(Resource resource) {
            this.resource = resource;
            return this;
        }
    }

    /**
     * Gets entry via identifier as Http GET request.
     */
    private class GetTodoBehavior extends AbstractTestBehavior {

        private String todoId;
        private String payloadData;
        private Resource resource;

        private Map<String, Object> validateExpressions = new LinkedHashMap<>();

        public GetTodoBehavior(String todoId) {
            this.todoId = todoId;
        }

        @Override
        public void apply() {
            http(httpActionBuilder -> httpActionBuilder
                .client(todoClient)
                .send()
                .get("/api/todo/" + todoId)
                .accept(ContentType.APPLICATION_JSON.getMimeType()));

            HttpClientResponseActionBuilder response = new HttpActionBuilder()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON);

            if (StringUtils.hasText(payloadData)) {
                response.payload(payloadData);
            } else if (resource != null) {
                response.payload(resource);
            }

            validateExpressions.forEach(response::validate);
        }

        GetTodoBehavior validate(String payload) {
            this.payloadData = payload;
            return this;
        }

        GetTodoBehavior validate(Resource resource) {
            this.resource = resource;
            return this;
        }

        GetTodoBehavior validate(String expression, Object expected) {
            validateExpressions.put(expression, expected);
            return this;
        }
    }
}
