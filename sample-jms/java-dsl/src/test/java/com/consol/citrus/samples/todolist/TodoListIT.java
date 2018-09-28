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
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    @Qualifier("todoJmsEndpoint")
    private JmsEndpoint todoJmsEndpoint;

    @Autowired
    @Qualifier("todoReportEndpoint")
    private JmsEndpoint todoReportEndpoint;

    @Autowired
    @Qualifier("todoJmsSyncEndpoint")
    private JmsEndpoint todoJmsSyncEndpoint;

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        send(todoJmsEndpoint)
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");

        http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .accept("text/html");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.XHTML)
            .xpath("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}");
    }

    @Test
    @CitrusTest
    public void testReportTodoEntryDone() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        send(todoJmsEndpoint)
                .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
                .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");

        echo("Set todo entry status to done");

        http()
            .client(todoClient)
            .send()
            .put("/api/todo/${todoId}")
            .queryParam("done", "true")
            .accept(MediaType.APPLICATION_JSON_VALUE);

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);

        echo("Trigger Jms report");

        http()
            .client(todoClient)
            .send()
            .get("/api/jms/report/done")
            .accept(MediaType.APPLICATION_JSON_VALUE);

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);

        receive(todoReportEndpoint)
                .messageType(MessageType.JSON)
                .payload("[{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"attachment\":null, \"done\":true}]")
                .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry");
    }

    @Test
    @CitrusTest
    public void testSyncAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        send(todoJmsSyncEndpoint)
                .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
                .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");

        receive(todoJmsSyncEndpoint)
                .payload("\"Message received\"");

        http()
                .client(todoClient)
                .send()
                .get("/todolist")
                .accept("text/html");

        http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.XHTML)
                .xpath("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}");
    }

}
