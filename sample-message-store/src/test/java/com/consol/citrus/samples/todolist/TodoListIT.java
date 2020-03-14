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
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testMessageStoreValidation() {
        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageName("todoRequest")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{\"id\": \"citrus:randomUUID()\", \"title\": \"citrus:concat('todo_', citrus:randomNumber(4))\", \"description\": \"ToDo Description\", \"done\": false}"));

        echo("citrus:message(todoRequest)");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageName("todoResponse")
            .messageType(MessageType.JSON)
            .validate("$.id", "citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')"));

        echo("citrus:message(todoResponse)");
    }

    @Test
    @CitrusTest
    public void testMessageStoreValidationCallback() {
        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageName("todoRequest")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{\"id\":\"citrus:randomUUID()\",\"title\":\"citrus:concat('todo_',citrus:randomNumber(4))\",\"description\":\"ToDo Description\",\"done\":false}"));

        echo("citrus:message(todoRequest)");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageName("todoResponse")
            .messageType(MessageType.JSON)
            .validationCallback((message, context) -> {
                Message todoRequest = context.getMessageStore().getMessage("todoRequest");
                Assert.assertEquals(message.getPayload(), todoRequest.getPayload());
            }));
    }

}
