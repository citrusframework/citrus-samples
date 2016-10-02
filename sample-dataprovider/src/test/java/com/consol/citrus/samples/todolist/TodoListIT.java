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
import com.consol.citrus.testng.CitrusParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private HttpClient todoClient;

    @Test(dataProvider = "todoDataProvider")
    @CitrusTest
    @CitrusParameters( { "todoName", "todoDescription" })
    public void testProvider(String todoName, String todoDescription) {
        variable("todoId", "citrus:randomUUID()");

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/todo/${todoId}")
            .accept("application/json");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
    }

    @DataProvider(name = "todoDataProvider")
    public Object[][] todoDataProvider() {
        return new Object[][] {
            new Object[] { "todo1", "Description: todo1" },
            new Object[] { "todo2", "Description: todo2" },
            new Object[] { "todo3", "Description: todo3" }
        };
    }

}
