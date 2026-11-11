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

package com.consol.citrus.samples.todolist.jms;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 */
@Component
@ConditionalOnProperty(prefix = "todo.jms", value = "enabled")
public class TodoJmsResource {

    @Autowired
    private TodoListService todoListService;

    @JmsListener(destination = "jms.todo.inbound", containerFactory = "jmsListenerContainerFactory")
    public void receiveTodo(TodoEntry todo) {
        todoListService.addEntry(todo);
    }

    @JmsListener(destination = "jms.todo.inbound.sync", containerFactory = "jmsListenerContainerFactory")
    @SendTo("jms.todo.inbound.sync.reply")
    public String receiveSynchronousTodo(TodoEntry todo) {
        receiveTodo(todo);
        return "Message received";
    }

}
