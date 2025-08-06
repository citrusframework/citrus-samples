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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import com.consol.citrus.samples.todolist.page.TodoPage;
import com.consol.citrus.samples.todolist.page.WelcomePage;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 */
public class TodoPagesIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private SeleniumBrowser browser;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testIndexPage() {
        $(selenium()
            .browser(browser)
            .start());

        $(selenium()
            .navigate(todoClient.getEndpointConfiguration().getRequestUrl()));

        $(http()
            .client(todoClient)
            .send()
            .delete("/api/todolist"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        WelcomePage welcomePage = new WelcomePage();

        $(selenium()
            .page(welcomePage)
            .validate());

        $(selenium()
            .page(welcomePage)
            .execute("startApp"));

        $(selenium()
            .page(new TodoPage())
            .validate());
    }

    @Test
    @CitrusTest
    public void testAddEntry() {
        variable("todoName", "todo_citrus:randomNumber(4)");
        variable("todoDescription", "Description: ${todoName}");

        $(http()
            .client(todoClient)
            .send()
            .delete("/api/todolist"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        $(selenium()
            .browser(browser)
            .start());

        $(selenium()
            .navigate(todoClient.getEndpointConfiguration().getRequestUrl() + "/todolist"));

        TodoPage todoPage = new TodoPage();

        $(selenium()
            .page(todoPage)
            .validate());

        $(selenium()
            .page(todoPage)
            .argument("${todoName}")
            .argument("${todoDescription}")
            .execute("submit"));

        $(selenium()
            .waitUntil()
            .element(By.xpath("(//li[@class='list-group-item']/span)[last()]"))
            .timeout(2000L)
            .visible());

        $(selenium()
            .find()
            .element(By.xpath("(//li[@class='list-group-item']/span)[last()]"))
            .text("${todoName}"));
    }
}
