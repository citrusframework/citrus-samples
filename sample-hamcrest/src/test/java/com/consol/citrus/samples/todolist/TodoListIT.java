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

import java.util.UUID;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.container.HamcrestConditionExpression.assertThat;
import static org.citrusframework.container.Iterate.Builder.iterate;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testHamcrestValidation() {
        String todoId = UUID.randomUUID().toString();

        variable("todoId", todoId);
        variable("todoName", "todo_${todoId}");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .validate(jsonPath()
                    .expression("$.keySet()", hasItems("id", "title", "description", "done"))
                    .expression("$.id", equalTo(todoId))
                    .expression("$.title", allOf(startsWith("todo_"), endsWith(todoId)))
                    .expression("$.description", anyOf(startsWith("Description:"), nullValue()))
                    .expression("$.done", not(true))));
    }

    @Test
    @CitrusTest
    public void testHamcrestCondition() {
        $(iterate()
            .condition(assertThat(lessThanOrEqualTo(5)))
            .actions(
                createVariable("todoId", "citrus:randomUUID()"),
                createVariable("todoName", "todo_${i}"),
                createVariable("todoDescription", "Description: ${todoName}"),
                http()
                    .client(todoClient)
                    .send()
                    .post("/api/todolist")
                    .message()
                    .type(MessageType.JSON)
                    .contentType(ContentType.APPLICATION_JSON.getMimeType())
                    .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"),
                http()
                    .client(todoClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .message()
                    .type(MessageType.PLAINTEXT)
                    .body("${todoId}")
        ));
    }
}
