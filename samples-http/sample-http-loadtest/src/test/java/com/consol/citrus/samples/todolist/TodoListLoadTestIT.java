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

import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test(invocationCount = 40, threadPoolSize = 4)
public class TodoListLoadTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @CitrusTest
    public void testAddTodo(@Optional @CitrusResource TestActionRunner actions) {
        System.out.println("testAddTodo " + actions.toString());

        actions.$(http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=citrus:concat('todo_', citrus:randomNumber(10))"));

        actions.$(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

    @CitrusTest
    public void testListTodos(@Optional @CitrusResource TestActionRunner actions) {
        System.out.println("testListTodos " + actions.toString());

        actions.$(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        actions.$(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }
}
