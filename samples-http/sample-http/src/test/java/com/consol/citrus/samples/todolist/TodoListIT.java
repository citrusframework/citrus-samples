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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.citrusframework.dsl.XpathSupport.xpath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testGet() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(xpath()
                        .expression("//xh:h1", "TODO list"))
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
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

        $(http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testTodoLifecycle() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        //Create
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

        //Verify existence
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
                .validate(jsonPath()
                        .expression("$.id", "${todoId}")
                        .expression("$.title", "${todoName}")
                        .expression("$.description", "${todoDescription}")
                        .expression("$.done", false)));

        //Delete
        $(http().client(todoClient)
                .send()
                .delete("/api/todo/${todoId}")
                .message()
                .accept("application/json"));

        $(http().client(todoClient)
                .receive()
                .response(HttpStatus.OK));
    }

}
