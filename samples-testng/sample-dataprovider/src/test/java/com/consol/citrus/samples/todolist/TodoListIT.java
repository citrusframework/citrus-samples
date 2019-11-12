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
import com.consol.citrus.testng.CitrusParameters;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Test(dataProvider = "todoDataProvider")
    @CitrusTest
    @CitrusParameters( { "todoName", "todoDescription", "done" })
    public void testProvider(String todoName, String todoDescription, boolean done) {
        variable("todoId", "citrus:randomUUID()");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @DataProvider(name = "todoDataProvider")
    public Object[][] todoDataProvider() {
        return new Object[][] {
            new Object[] { "todo1", "Description: todo1", false },
            new Object[] { "todo2", "Description: todo2", true },
            new Object[] { "todo3", "Description: todo3", false }
        };
    }

}
