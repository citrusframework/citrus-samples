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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private WebServiceClient todoClient;

    @Autowired
    private WebServiceServer todoServer;

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(soap()
            .client(todoClient)
            .send()
            .fork(true)
            .message()
            .soapAction("addTodoEntry")
            .body(Resources.fromClasspath("templates/addTodoEntryRequest.xml")));

        $(soap()
            .server(todoServer)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/addTodoEntryRequest.xml"))
            .header(Resources.fromClasspath("templates/soapWsAddressingHeader.xml")));

        $(soap()
            .server(todoServer)
            .send()
            .message()
            .body(Resources.fromClasspath("templates/addTodoEntryResponse.xml")));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/addTodoEntryResponse.xml")));

        $(soap()
            .client(todoClient)
            .send()
            .fork(true)
            .message()
            .soapAction("getTodoList")
            .body(Resources.fromClasspath("templates/getTodoListRequest.xml")));

        $(soap()
            .server(todoServer)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/getTodoListRequest.xml"))
            .header(Resources.fromClasspath("templates/soapWsAddressingHeader.xml")));

        $(soap()
            .server(todoServer)
            .send()
            .message()
            .body(Resources.fromClasspath("templates/getTodoListResponse.xml")));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/getTodoListResponse.xml")));
    }

}
