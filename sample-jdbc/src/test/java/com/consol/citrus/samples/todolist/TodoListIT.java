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
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    /** Jdbc database operation marshaller */
    private ObjectMapper jdbcMarshaller = new JdbcMarshaller();

    @Test
    @CitrusTest
    public void testIndexPage() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .fork(true)
            .accept("text/html");

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .payload(new Operation(new Execute(new Execute.Statement("SELECT id, title, description FROM todo_entries"))), jdbcMarshaller);

        send(jdbcServer)
                .payload(createTodoListResultSet(), jdbcMarshaller);

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

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .fork(true)
            .contentType("application/x-www-form-urlencoded")
            .payload("title=${todoName}&description=${todoDescription}");

        receive(jdbcServer)
            .messageType(MessageType.JSON)
            .payload(new Operation(new Execute(new Execute.Statement("INSERT INTO todo_entries (id, title, description, done) VALUES (?, ?, ?, ?)"))), jdbcMarshaller);

        send(jdbcServer)
            .payload(new OperationResult(true), jdbcMarshaller);

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
                .payload(new Operation(new Execute(new Execute.Statement("SELECT id, title, description FROM todo_entries"))), jdbcMarshaller);

        send(jdbcServer)
                .payload(createTodoListResultSet(), jdbcMarshaller);

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.XHTML)
            .xpath("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}");
    }

    /**
     * Creates sample result set with mocked todolist entries.
     * @return
     */
    private OperationResult createTodoListResultSet() {
        OperationResult result = new OperationResult(true);
        ResultSet resultSet = new ResultSet(1)
                .columns(new ResultSet.Column("id"), new ResultSet.Column("title"), new ResultSet.Column("description"), new ResultSet.Column("done"))
                .rows(new ResultSet.Row(UUID.randomUUID().toString(), "${todoName}", "${todoDescription}", "false"));
        result.setResultSet(resultSet);
        return result;
    }

}
