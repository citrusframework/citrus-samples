/*
 * Copyright 2006-2018 the original author or authors.
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
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testStoredProcedureCallJson() {
        variable("todoId", "citrus:randomUUID()");
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
            .get("api/todolist/1")
            .fork(true));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.createCallableStatement("{CALL limitedToDoList(?)}")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("{CALL limitedToDoList(?)} - (1)")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.success().dataSet("[ {" +
                    "\"id\": \"${todoId}\"," +
                    "\"title\": \"${todoName}\"," +
                    "\"description\": \"${todoDescription}\"," +
                    "\"done\": \"false\"" +
                    "} ]")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.closeStatement()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .payload("[ {" +
                        "\"id\": \"${todoId}\"," +
                        "\"title\": \"${todoName}\"," +
                        "\"description\": \"${todoDescription}\"," +
                        "\"done\": false" +
                    "} ]"));
    }

    @Test
    @CitrusTest
    public void testStoredProcedureCallXml() {
        variable("todoId", "citrus:randomUUID()");
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
            .get("api/todolist/1")
            .fork(true));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.createCallableStatement("{CALL limitedToDoList(?)}")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("{CALL limitedToDoList(?)} - (1)")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.XML)
            .message(JdbcMessage.success().dataSet("" +
                    "<dataset>" +
                        "<row>" +
                            "<id>${todoId}</id>"+
                            "<title>${todoName}</title>"+
                            "<description>${todoDescription}</description>" +
                            "<done>false</done>" +
                         "</row>" +
                    "</dataset>")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.closeStatement()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .payload("[ {" +
                    "\"id\": \"${todoId}\"," +
                    "\"title\": \"${todoName}\"," +
                    "\"description\": \"${todoDescription}\"," +
                    "\"done\": false" +
                    "} ]"));
    }

    @Test
    @CitrusTest
    public void testStoredProcedureCallFailed() {
        waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl());

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("api/todolist/1")
            .fork(true));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.createCallableStatement("{CALL limitedToDoList(?)}")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("{CALL limitedToDoList(?)} - (1)")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.error().exception("Error in called procedure")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.closeStatement()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @CitrusTest
    public void testStoredProcedureNotFound() {
        waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl());

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("api/todolist/1")
            .fork(true));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.createCallableStatement("{CALL limitedToDoList(?)}")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.error().exception("Could not find procedure 'limitedToDoList'")));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
