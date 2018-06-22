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
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.UUID;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private DataSource todoDataSource;

    @Test
    @CitrusTest
    public void testIndexPage() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        waitFor().http(todoClient.getEndpointConfiguration().getRequestUrl())
                .status(HttpStatus.OK)
                .method(HttpMethod.GET)
                .ms(20000L)
                .interval(1000L);

        http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .fork(true)
            .accept("text/html");

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries"));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet("[ {" +
                            "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                            "\"title\": \"${todoName}\"," +
                            "\"description\": \"${todoDescription}\"," +
                            "\"done\": \"false\"" +
                        "} ]"));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.XHTML)
            .xpath("//xh:h1", "TODO list")
            .payload("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                    "\"org/w3/xhtml/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                      "<head>@ignore@</head>" +
                      "<body>" +
                        "<div class=\"container-fluid\">" +
                          "<div class=\"row\">" +
                            "<div class=\"@ignore@\">" +
                              "<h1>TODO list</h1>" +
                                "<ul class=\"list-group\">" +
                                  "<li class=\"list-group-item\">" +
                                    "<input class=\"complete\" id=\"@ignore@\" name=\"complete\" type=\"checkbox\" />" +
                                    "<span>${todoName}</span>" +
                                    "<a class=\"@ignore@\" id=\"@ignore@\" title=\"Remove todo\">" +
                                        "<span style=\"color: #A50000;\">x</span>" +
                                    "</a>" +
                                  "</li>" +
                                "</ul>" +
                              "<h2>New TODO entry</h2>" +
                              "<form method=\"post\">@ignore@</form>" +
                            "</div>" +
                          "</div>" +
                        "</div>" +
                      "</body>" +
                    "</html>");
    }

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        waitFor().http(todoClient.getEndpointConfiguration().getRequestUrl())
                .status(HttpStatus.OK)
                .method(HttpMethod.GET)
                .ms(20000L)
                .interval(1000L);

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .contentType("application/x-www-form-urlencoded")
            .payload("title=${todoName}&description=${todoDescription}");

        receive(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@"));

        send(jdbcServer)
            .message(JdbcMessage.success().rowsUpdated(1));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND);

        http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .fork(true)
            .accept("text/html");

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries"));

        send(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.success().dataSet("[ {" +
                            "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                            "\"title\": \"${todoName}\"," +
                            "\"description\": \"${todoDescription}\"," +
                            "\"done\": \"false\"" +
                        "} ]"));

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.XHTML)
            .xpath("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}");
    }

    @Test
    @CitrusTest
    public void testException() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        waitFor().http(todoClient.getEndpointConfiguration().getRequestUrl())
                .status(HttpStatus.OK)
                .method(HttpMethod.GET)
                .ms(20000L)
                .interval(1000L);

        http()
                .client(todoClient)
                .send()
                .post("/todolist")
                .fork(true)
                .contentType("application/x-www-form-urlencoded")
                .payload("title=${todoName}&description=${todoDescription}");

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)')@"));

        send(jdbcServer)
                .message(JdbcMessage.error().exception("Something went wrong"));

        http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
