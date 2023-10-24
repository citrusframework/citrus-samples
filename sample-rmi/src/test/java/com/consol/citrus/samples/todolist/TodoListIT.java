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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.message.RmiMessage;
import org.citrusframework.rmi.server.RmiServer;
import com.consol.citrus.samples.todolist.remote.TodoListService;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private RmiClient todoRmiClient;

    @Autowired
    private RmiServer todoRmiServer;

    @Test
    @CitrusTest
    public void testAddTodo() {
        $(send()
            .endpoint(todoRmiClient)
            .fork(true)
            .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                    .argument("todo-star")
                    .argument("Star me on github")));

        $(receive()
            .endpoint(todoRmiServer)
            .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                    .argument("todo-star")
                    .argument("Star me on github")));

        $(send()
            .endpoint(todoRmiServer)
            .message(RmiMessage.result()));

        $(receive()
            .endpoint(todoRmiClient)
            .message(RmiMessage.result()));
    }

    @Test
    @CitrusTest
    public void testGetTodos() {
        $(send()
            .endpoint(todoRmiClient)
            .fork(true)
            .message(RmiMessage.invocation(TodoListService.class, "getTodos")));

        $(receive()
            .endpoint(todoRmiServer)
            .message(RmiMessage.invocation(TodoListService.class, "getTodos")));

        $(send()
            .endpoint(todoRmiServer)
            .message()
            .body("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                        "<object type=\"java.util.Map\" value=\"{todo-follow=Follow us on github}\"/>" +
                    "</service-result>"));

        $(receive()
            .endpoint(todoRmiClient)
            .message()
            .body("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                        "<object type=\"java.util.LinkedHashMap\" value=\"{todo-follow=Follow us on github}\"/>" +
                    "</service-result>"));
    }

}
