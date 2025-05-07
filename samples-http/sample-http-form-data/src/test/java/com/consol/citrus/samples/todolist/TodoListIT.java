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
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.model.Control;
import org.citrusframework.http.model.FormData;
import org.citrusframework.http.model.FormMarshaller;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.message.builder.MarshallingPayloadBuilder.Builder.marshal;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private HttpServer todoListServer;

    @Test
    @CitrusTest
    public void testPlainFormData() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(http()
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .type(MessageType.PLAINTEXT)
            .body("{description=[${todoDescription}], title=[${todoName}]}"));

        $(http()
            .server(todoListServer)
            .respond(HttpStatus.OK));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testFormData() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(http()
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .type(MessageType.FORM_URL_ENCODED)
            .body(marshal(getFormData(), new FormMarshaller())));

        $(http()
            .server(todoListServer)
            .respond(HttpStatus.OK));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    private FormData getFormData() {
        FormData formData = new FormData();

        formData.setAction("/api/todo");
        formData.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        Control title = new Control();
        title.setName("title");
        title.setValue("${todoName}");

        Control description = new Control();
        description.setName("description");
        description.setValue("@ignore@");

        formData.addControl(description);
        formData.addControl(title);

        return formData;
    }

    @Test
    @CitrusTest
    public void testFormDataXml() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(http()
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .type(MessageType.FORM_URL_ENCODED)
            .body("<form-data xmlns=\"http://www.citrusframework.org/schema/http/message\">" +
                        "<content-type>application/x-www-form-urlencoded</content-type>" +
                        "<action>/api/todo</action>" +
                        "<controls>" +
                            "<control name=\"description\">" +
                                "<value>${todoDescription}</value>" +
                            "</control>" +
                            "<control name=\"title\">" +
                                "<value>${todoName}</value>" +
                            "</control>" +
                        "</controls>" +
                    "</form-data>"));

        $(http()
            .server(todoListServer)
            .respond(HttpStatus.OK));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

}
