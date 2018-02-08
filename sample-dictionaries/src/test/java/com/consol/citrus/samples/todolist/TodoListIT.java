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
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    @Qualifier("inboundDictionary")
    private DataDictionary inboundDictionary;

    @Autowired
    @Qualifier("outboundDictionary")
    private DataDictionary outboundDictionary;

    @Test
    @CitrusTest
    public void testJsonPayloadValidation() {
        variable("todoId", "citrus:randomUUID()");

        http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}");

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
            .dictionary(inboundDictionary)
            .payload("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}");
    }

    @Test
    @CitrusTest
    public void testJsonValidationWithFileResource() {
        variable("todoId", "citrus:randomUUID()");

        http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType("application/json")
            .payload(new ClassPathResource("templates/todo.json"));

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
            .dictionary(inboundDictionary)
            .payload(new ClassPathResource("templates/todo.json"));
    }

    @Test
    @CitrusTest
    public void testJsonPathValidation() {
        variable("todoId", "citrus:randomUUID()");

        http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .dictionary(outboundDictionary)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}");

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
            .dictionary(inboundDictionary)
            .validate("$.id", "${todoId}")
            .validate("$.title", "todo_${todoId}")
            .validate("$.description", "@endsWith('todo_${todoId}')@")
            .validate("$.done", false);
    }

}
