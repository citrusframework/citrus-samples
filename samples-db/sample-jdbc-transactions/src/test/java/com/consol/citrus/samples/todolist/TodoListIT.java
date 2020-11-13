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
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private JdbcServer jdbcServer;


    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testTransaction() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl());

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.startTransaction()));

        send(sendMessageBuilder -> sendMessageBuilder.endpoint(jdbcServer).message(JdbcMessage.success()));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().rowsUpdated(1)));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.commitTransaction()));

        send(sendMessageBuilder -> sendMessageBuilder.endpoint(jdbcServer).message(JdbcMessage.success()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

    @Test
    @CitrusTest
    public void testRollback() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl());

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.startTransaction()));

        send(sendMessageBuilder -> sendMessageBuilder.endpoint(jdbcServer).message(JdbcMessage.success()));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.error().exception("Could not execute something")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.rollbackTransaction()));

        send(sendMessageBuilder -> sendMessageBuilder.endpoint(jdbcServer).message(JdbcMessage.success()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @CitrusTest
    public void testWithoutTransactionVerification() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        jdbcServer.getEndpointConfiguration().setAutoTransactionHandling(true);

        waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl());

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().rowsUpdated(1)));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

    @AfterTest
    public void resetTransactionState(){
        jdbcServer.getEndpointConfiguration().setAutoTransactionHandling(false);
    }
}
