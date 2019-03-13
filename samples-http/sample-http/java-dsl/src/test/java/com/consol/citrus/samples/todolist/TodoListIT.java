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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testGet() {
        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/todolist")
            .accept(MediaType.TEXT_HTML_VALUE));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.XHTML)
            .xpath("//xh:h1", "TODO list")
            .payload("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                    "\"org/w3/xhtml/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                        "<head>@ignore@</head>" +
                        "<body>@ignore@</body>" +
                    "</html>"));
    }

    @Test
    @CitrusTest
    public void testPost() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/todolist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

    @Test
    @CitrusTest
    public void testTodoLifecycle() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        //Create
        http(http -> http.client(todoClient)
                .send()
                .post("/api/todolist")
                .messageType(MessageType.JSON)
                .contentType("application/json")
                .payload("{ \"id\": \"${todoId}\", " +
                        "\"title\": \"${todoName}\", " +
                        "\"description\": \"${todoDescription}\", " +
                        "\"done\": false}"));

        http(http -> http.client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("${todoId}"));

        //Verify existence
        http(http -> http.client(todoClient)
                .send()
                .get("/api/todo/${todoId}")
                .accept("application/json"));

        http(http -> http.client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .validate("$.id", "${todoId}")
                .validate("$.title", "${todoName}")
                .validate("$.description", "${todoDescription}")
                .validate("$.done", false));

        //Delete
        http(http -> http.client(todoClient)
                .send()
                .delete("/api/todo/${todoId}")
                .accept("application/json"));

        http(http -> http.client(todoClient)
                .receive()
                .response(HttpStatus.OK));
    }

}
