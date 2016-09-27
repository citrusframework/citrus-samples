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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.message.*;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.context.DefaultValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JmsEndpoint todoJmsEndpoint;

    @Test
    @CitrusTest
    public void testAddTodoEntryBinary() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        send(todoJmsEndpoint)
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .messageType(MessageType.BINARY)
            .message(new DefaultMessage("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }".getBytes()));

        receive(todoJmsEndpoint)
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .messageType(MessageType.BINARY)
            .validator(new BinaryMessageValidator())
            .message(new DefaultMessage("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }".getBytes()));
    }

    /**
     * Validates binary message content.
     */
    private class BinaryMessageValidator extends AbstractMessageValidator<DefaultValidationContext> {

        @Override
        public void validateMessage(Message receivedMessage, Message controlMessage,
                                    TestContext context, DefaultValidationContext validationContext) {
            Assert.isTrue(new String(receivedMessage.getPayload(byte[].class))
                    .equals(new String(controlMessage.getPayload(byte[].class))), "Binary message validation failed!");
        }

        @Override
        public boolean supportsMessageType(String messageType, Message message) {
            return messageType.equalsIgnoreCase(MessageType.BINARY.name());
        }

        @Override
        protected Class getRequiredValidationContextType() {
            return DefaultValidationContext.class;
        }
    }

}
