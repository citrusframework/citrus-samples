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

import java.util.UUID;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Wait.Builder.waitFor;
import static com.consol.citrus.dsl.XpathSupport.xpath;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testIndexPage() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl()));

        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .fork(true)
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(receive()
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries"))
            .type(MessageType.JSON));

        $(send()
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().dataSet("[ {" +
                        "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                        "\"title\": \"${todoName}\"," +
                        "\"description\": \"${todoDescription}\"," +
                        "\"done\": \"false\"" +
                    "} ]"))
            .type(MessageType.JSON));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(xpath()
                        .expression("//xh:h1", "TODO list"))
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
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
                    "</html>"));
    }

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl()));

        $(http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(receive()
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries " +
                    "(id, title, description, done) VALUES (?, ?, ?, ?)')@"))
            .type(MessageType.JSON));

        $(send()
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().rowsUpdated(1)));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));

        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .fork(true)
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(receive()
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries"))
            .type(MessageType.JSON));

        $(send()
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().dataSet("[ {" +
                        "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                        "\"title\": \"${todoName}\"," +
                        "\"description\": \"${todoDescription}\"," +
                        "\"done\": \"false\"" +
                    "} ]"))
            .type(MessageType.JSON));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(xpath()
                        .expression("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}")));
    }

    @Test
    @CitrusTest
    public void testException() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(waitFor().http()
                .status(HttpStatus.OK.value())
                .method(HttpMethod.GET.name())
                .milliseconds(20000L)
                .interval(1000L)
                .url(todoClient.getEndpointConfiguration().getRequestUrl()));

        $(http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(receive()
            .endpoint(jdbcServer)
            .message(JdbcMessage.execute("@startsWith('INSERT INTO todo_entries " +
                    "(id, title, description, done) VALUES (?, ?, ?, ?)')@"))
            .type(MessageType.JSON));

        $(send()
            .endpoint(jdbcServer)
            .message(JdbcMessage.error().exception("Something went wrong")));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
