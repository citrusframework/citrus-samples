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

import java.util.Map;
import java.util.UUID;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @CitrusTest
    public void testObjectMapping() {
        final UUID uuid = UUID.randomUUID();
        variable("todoId", uuid.toString());
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body(new ObjectMappingPayloadBuilder(new TodoEntry(uuid, "${todoName}", "${todoDescription}"), objectMapper)));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .validate(new JsonMappingValidationProcessor<>(TodoEntry.class, objectMapper) {
                @Override
                public void validate(TodoEntry todoEntry, Map<String, Object> headers, TestContext context) {
                    Assert.assertNotNull(todoEntry);
                    Assert.assertEquals(todoEntry.getId(), uuid);
                }
            }));
    }

}
