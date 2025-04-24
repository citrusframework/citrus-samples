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

import javax.sql.DataSource;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.xml.XpathMessageValidationContext.Builder.xpath;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private DataSource todoDataSource;

    @Test
    @CitrusTest
    public void testIndexPage() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(xpath().expression("//xh:h1", "TODO list"))
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                    "\"org/w3/xhtml/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                        "<head>@ignore@</head>" +
                        "<body>@ignore@</body>" +
                    "</html>"));
    }

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(query(todoDataSource)
            .statement("select count(*) as cnt from todo_entries where title = '${todoName}'")
            .validate("cnt", "0"));

        $(http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        $(http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(xpath().expression("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}")));

        $(query(todoDataSource)
            .statement("select count(*) as cnt from todo_entries where title = '${todoName}'")
            .validate("cnt", "1"));
    }

}
