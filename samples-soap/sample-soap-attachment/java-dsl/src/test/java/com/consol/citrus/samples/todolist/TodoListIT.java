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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private WebServiceClient todoClient;

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        soap(soapActionBuilder -> soapActionBuilder
            .client(todoClient)
            .send()
            .soapAction("addTodoEntry")
            .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"))
            .attachment("myAttachment", "text/plain", "This is my attachment"));

        soap(soapActionBuilder -> soapActionBuilder
            .client(todoClient)
            .receive()
            .payload(new ClassPathResource("templates/addTodoEntryResponse.xml")));

        soap(soapActionBuilder -> soapActionBuilder
            .client(todoClient)
            .send()
            .soapAction("getTodoList")
            .payload(new ClassPathResource("templates/getTodoListRequest.xml")));

        soap(soapActionBuilder -> soapActionBuilder
            .client(todoClient)
            .receive()
            .payload(new ClassPathResource("templates/getTodoListResponse.xml")));
    }

}
