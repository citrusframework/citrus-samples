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
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.message.MailMessage;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

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

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/reporting/mail");

        echo("Receive reporting mail");

        receive(mailServer)
            .message(MailMessage.request()
                    .from("todo-report@example.org")
                    .to("users@example.org")
                    .cc("")
                    .bcc("")
                    .subject("ToDo report")
                    .body("There are '1' todo entries!", "text/plain; charset=us-ascii"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report");

        send(mailServer)
            .message(MailMessage.response(250, "OK"));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);
    }

    @Test
    @CitrusTest
    public void testMailReportXml() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        mailServer.getMarshaller().setType(MessageType.XML.name());

        clearTodoList();

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        variable("entryCount", "1");

        http()
            .client(todoClient)
            .send()
            .get("/reporting/mail");

        echo("Receive reporting mail");

        receive(mailServer)
            .payload(new ClassPathResource("templates/mail.xml"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report");

        send(mailServer)
            .payload(new ClassPathResource("templates/mail-response.xml"));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);
    }

    @Test
    @CitrusTest
    public void testMailReportJson() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        mailServer.getMarshaller().setType(MessageType.JSON.name());

        clearTodoList();

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        variable("entryCount", "1");

        http()
            .client(todoClient)
            .send()
            .get("/reporting/mail");

        echo("Receive reporting mail");

        receive(mailServer)
            .messageType(MessageType.JSON)
            .payload(new ClassPathResource("templates/mail.json"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report");

        send(mailServer)
            .payload(new ClassPathResource("templates/mail-response.json"));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);
    }

    /**
     * Remove all entries from todolist.
     */
    private void clearTodoList() {
        http()
            .client(todoClient)
            .send()
            .delete("/todolist");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND);
    }

}
