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

import org.apache.hc.core5.http.ContentType;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testXmlDomTreeValidation() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .contentType(ContentType.APPLICATION_XML.getMimeType())
            .body("<todo xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<id>${todoId}</id>" +
                        "<title>${todoName}</title>" +
                        "<description>${todoDescription}</description>" +
                    "</todo>"));

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
            .message()
            .body("<todo xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<id>${todoId}</id>" +
                        "<title>${todoName}</title>" +
                        "<description>${todoDescription}</description>" +
                        "<done>false</done>" +
                    "</todo>"));
    }

    @Test
    @CitrusTest
    public void testXmlValidationWithFileResource() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .contentType(ContentType.APPLICATION_XML.getMimeType())
            .body(Resources.fromClasspath("templates/todo.xml")));

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
            .message()
            .body(Resources.fromClasspath("templates/todo.xml")));
    }

    @Test
    @CitrusTest
    public void testXpathValidation() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .contentType(ContentType.APPLICATION_XML.getMimeType())
            .body("<todo xmlns=\"http://citrusframework.org/samples/todolist\">" +
                        "<id>${todoId}</id>" +
                        "<title>${todoName}</title>" +
                        "<description>${todoDescription}</description>" +
                    "</todo>"));

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
            .message()
            .validate(validation().xpath()
                    .expression("/t:todo/t:id", "${todoId}")
                    .expression("/t:todo/t:title", "${todoName}")
                    .expression("/t:todo/t:description", "${todoDescription}")
                    .expression("/t:todo/t:done", "false")));
    }

}
