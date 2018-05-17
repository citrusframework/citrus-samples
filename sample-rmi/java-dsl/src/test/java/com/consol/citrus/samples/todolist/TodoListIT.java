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
import com.consol.citrus.rmi.client.RmiClient;
import com.consol.citrus.rmi.message.RmiMessage;
import com.consol.citrus.rmi.server.RmiServer;
import com.consol.citrus.samples.todolist.remote.TodoListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private RmiClient todoRmiClient;

    @Autowired
    private RmiServer todoRmiServer;

    @Test
    @CitrusTest
    public void testAddTodo() {
        send(todoRmiClient)
            .fork(true)
            .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                    .argument("todo-star")
                    .argument("Star me on github"));

        receive(todoRmiServer)
            .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                    .argument("todo-star")
                    .argument("Star me on github"));

        send(todoRmiServer)
            .message(RmiMessage.result());

        receive(todoRmiClient)
            .message(RmiMessage.result());
    }

    @Test
    @CitrusTest
    public void testGetTodos() {
        send(todoRmiClient)
                .fork(true)
                .message(RmiMessage.invocation(TodoListService.class, "getTodos"));

        receive(todoRmiServer)
                .message(RmiMessage.invocation(TodoListService.class, "getTodos"));

        send(todoRmiServer)
                .payload("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                            "<object type=\"java.util.Map\" value=\"{todo-follow=Follow us on github}\"/>" +
                        "</service-result>");

        receive(todoRmiClient)
                .payload("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                            "<object type=\"java.util.LinkedHashMap\" value=\"{todo-follow=Follow us on github}\"/>" +
                        "</service-result>");
    }

}
