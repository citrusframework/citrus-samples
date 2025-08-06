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
import org.citrusframework.mail.message.CitrusMailMessageHeaders;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;
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

    @Autowired
    private MailServer mailServer;

    @Test
    @CitrusTest
    public void testMailReport() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        clearTodoList();

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}"));

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
            .get("/api/reporting/mail"));

        $(echo("Receive reporting mail"));

        $(receive()
            .endpoint(mailServer)
            .message(MailMessage.request()
                .from("todo-report@example.org")
                .to("users@example.org")
                .subject("ToDo report")
                .body("There are '1' todo entries!", "text/plain; charset=us-ascii"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report"));

        $(send()
            .endpoint(mailServer)
            .message(MailMessage.response(250, "OK")));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testMailReportXml() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        mailServer.getMarshaller().setType(MessageType.XML.name());

        clearTodoList();

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        variable("entryCount", "1");

        $(http()
            .client(todoClient)
            .send()
            .get("/api/reporting/mail"));

        $(echo("Receive reporting mail"));

        $(receive()
            .endpoint(mailServer)
            .message()
            .body(Resources.fromClasspath("templates/mail.xml"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report"));

        $(send()
            .endpoint(mailServer)
            .message()
            .body(Resources.fromClasspath("templates/mail-response.xml")));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testMailReportJson() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        mailServer.getMarshaller().setType(MessageType.JSON.name());

        clearTodoList();

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        variable("entryCount", "1");

        $(http()
            .client(todoClient)
            .send()
            .get("/api/reporting/mail"));

        $(echo("Receive reporting mail"));

        $(receive()
            .endpoint(mailServer)
            .message()
            .type(MessageType.JSON)
            .body(Resources.fromClasspath("templates/mail.json"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report"));

        $(send()
            .endpoint(mailServer)
            .message()
            .body(Resources.fromClasspath("templates/mail-response.json")));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    /**
     * Remove all entries from todolist.
     */
    private void clearTodoList() {
        $(http()
            .client(todoClient)
            .send()
            .delete("/api/todolist"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

}
