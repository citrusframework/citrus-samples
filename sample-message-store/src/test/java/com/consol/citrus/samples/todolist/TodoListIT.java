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
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.dsl.JsonPathSupport.jsonPath;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testMessageStoreValidation() {
        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .name("todoRequest")
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{\"id\": \"citrus:randomUUID()\", \"title\": \"citrus:concat('todo_', citrus:randomNumber(4))\", \"description\": \"ToDo Description\", \"done\": false}"));

        $(echo("citrus:message(todoRequest)"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("citrus:jsonPath(citrus:message(todoRequest.body()), '$.id')"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/citrus:jsonPath(citrus:message(todoRequest.body()), '$.id')")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .name("todoResponse")
            .type(MessageType.JSON)
            .validate(jsonPath()
                        .expression("$.id", "citrus:jsonPath(citrus:message(todoRequest.body()), '$.id')")));

        $(echo("citrus:message(todoResponse)"));
    }

    @Test
    @CitrusTest
    public void testMessageStoreValidationCallback() {
        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .name("todoRequest")
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{\"id\":\"citrus:randomUUID()\",\"title\":\"citrus:concat('todo_',citrus:randomNumber(4))\",\"description\":\"ToDo Description\",\"done\":false}"));

        $(echo("citrus:message(todoRequest)"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("citrus:jsonPath(citrus:message(todoRequest.body()), '$.id')"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/citrus:jsonPath(citrus:message(todoRequest.body()), '$.id')")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .name("todoResponse")
            .type(MessageType.JSON)
            .validate((message, context) -> {
                Message todoRequest = context.getMessageStore().getMessage("todoRequest");
                Assert.assertEquals(message.getPayload(), todoRequest.getPayload());
            }));
    }

}
