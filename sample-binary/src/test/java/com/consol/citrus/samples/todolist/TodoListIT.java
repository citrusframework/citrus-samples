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

import java.nio.charset.StandardCharsets;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.validation.interceptor.BinaryMessageProcessor.Builder.toBinary;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private JmsEndpoint todoJmsEndpoint;

    @Test
    @CitrusTest
    public void testAddTodoEntryBinaryBase64() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        $(send()
            .endpoint(todoJmsEndpoint)
            .message()
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}")
            .process(toBinary().encoding(StandardCharsets.UTF_8)));

        $(receive()
            .endpoint(todoJmsEndpoint)
            .message()
            .type(MessageType.BINARY_BASE64)
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .body("citrus:encodeBase64('{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}')"));
    }

    @Test
    @CitrusTest
    public void testAddTodoEntryBinary() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        $(send()
            .endpoint(todoJmsEndpoint)
            .message()
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}")
            .process(toBinary().encoding(StandardCharsets.UTF_8)));

        $(receive()
            .endpoint(todoJmsEndpoint)
            .message()
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .type(MessageType.BINARY)
            .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

}
