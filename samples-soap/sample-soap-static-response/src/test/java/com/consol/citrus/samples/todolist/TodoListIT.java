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
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient todoClient;

    @Test
    @CitrusTest
    public void testTodo() {
        $(soap()
            .client(todoClient)
            .send()
            .message()
            .soapAction("getTodo")
            .body("<todo:getTodoRequest xmlns:todo=\"http://citrusframework.org/samples/todolist\"></todo:getTodoRequest>"));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .body("<getTodoResponse xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<todoEntry>" +
                            "<id>${todoId}</id>" +
                            "<title>${todoName}</title>" +
                            "<description>${todoDescription}</description>" +
                            "<done>false</done>" +
                        "</todoEntry>" +
                    "</getTodoResponse>"));
    }

    @Test
    @CitrusTest
    public void testTodoList() {
        $(soap()
            .client(todoClient)
            .send()
            .message()
            .soapAction("getTodoList")
            .body("<todo:getTodoListRequest xmlns:todo=\"http://citrusframework.org/samples/todolist\"></todo:getTodoListRequest>"));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .body("<getTodoListResponse xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<list>" +
                            "<todoEntry>" +
                                "<id>${todoId}</id>" +
                                "<title>${todoName}</title>" +
                                "<description>${todoDescription}</description>" +
                                "<done>false</done>" +
                            "</todoEntry>" +
                        "</list>" +
                    "</getTodoListResponse>"));
    }

}
