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

import com.consol.citrus.annotations.*;
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusExtension.class)
public class TodoListIT {

    @CitrusEndpoint
    private HttpClient todoClient;

    @Test
    @CitrusTest
    void testGet(@CitrusResource TestRunner runner) {
        runner.http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/todolist")
            .accept(MediaType.TEXT_HTML_VALUE));

        runner.http(httpActionBuilder -> httpActionBuilder
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
    void testPost(@CitrusResource TestRunner runner) {
        runner.variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        runner.variable("todoDescription", "Description: ${todoName}");

        runner.http(action -> action
            .client(todoClient)
            .send()
            .post("/todolist")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        runner.http(action -> action
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }
}
