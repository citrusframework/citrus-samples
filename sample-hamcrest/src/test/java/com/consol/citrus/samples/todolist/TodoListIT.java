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
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.*;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testHamcrestValidation() {
        String todoId = UUID.randomUUID().toString();

        variable("todoId", todoId);
        variable("todoName", "todo_${todoId}");
        variable("todoDescription", "Description: ${todoName}");

        http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept("application/json");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .validate("$.keySet()", hasItems("id", "title", "description", "done"))
            .validate("$.id", equalTo(todoId))
            .validate("$.title", allOf(startsWith("todo_"), endsWith(todoId)))
            .validate("$.description", anyOf(startsWith("Description:"), nullValue()))
            .validate("$.done", not(true));
    }

    @Test
    @CitrusTest
    public void testHamcrestCondition() {
        iterate()
            .condition(lessThanOrEqualTo(5))
            .actions(
                createVariable("todoId", "citrus:randomUUID()"),
                createVariable("todoName", "todo_${i}"),
                createVariable("todoDescription", "Description: ${todoName}"),
                http()
                    .client(todoClient)
                    .send()
                    .post("/api/todolist")
                    .messageType(MessageType.JSON)
                    .contentType("application/json")
                    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"),

                http()
                    .client(todoClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("${todoId}")
        );
    }
}
