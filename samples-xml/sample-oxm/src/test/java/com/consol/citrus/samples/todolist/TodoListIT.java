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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.message.builder.MarshallingPayloadBuilder.Builder.marshal;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private Jaxb2Marshaller marshaller;

    @Test
    @CitrusTest
    public void testObjectMarshalling() {
        final UUID uuid = UUID.randomUUID();
        variable("todoId", uuid.toString());
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .contentType(ContentType.APPLICATION_XML.getMimeType())
            .body(marshal(new TodoEntry(uuid, "${todoName}", "${todoDescription}"), marshaller)));

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
            .accept(ContentType.APPLICATION_XML.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .validate(new XmlMarshallingValidationProcessor<TodoEntry>(marshaller) {
                @Override
                public void validate(TodoEntry todoEntry, Map<String, Object> headers, TestContext context) {
                    Assert.assertNotNull(todoEntry);
                    Assert.assertEquals(todoEntry.getId(), uuid);
                }
            }));
    }

}
