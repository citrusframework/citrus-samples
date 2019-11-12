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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private SeleniumBrowser browser;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testIndexPage() {
        selenium(seleniumActionBuilder -> seleniumActionBuilder
                .browser(browser)
                .start());

        selenium(seleniumActionBuilder -> seleniumActionBuilder
                .navigate(todoClient.getEndpointConfiguration().getRequestUrl()));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
                .find()
                .enabled(true)
                .displayed(true)
                .element(By.linkText("Run application")));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
                .click()
                .element(By.linkText("Run application")));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
                .find()
                .element(By.tagName("h1"))
                .text("TODO list"));
    }

    @Test
    @CitrusTest
    public void testAddEntry() {
        variable("todoName", "todo_citrus:randomNumber(4)");
        variable("todoDescription", "Description: ${todoName}");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .delete("/api/todolist"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .browser(browser)
            .start());

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .navigate(todoClient.getEndpointConfiguration().getRequestUrl() + "/todolist"));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .find()
            .element(By.xpath("(//li[@class='list-group-item'])[last()]"))
            .text("No todos found"));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .setInput("${todoName}")
            .element(By.name("title")));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .setInput("${todoDescription}")
            .element(By.name("description"))
        );

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .click()
            .element(By.tagName("button")));

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .waitUntil()
            .element(By.xpath("(//li[@class='list-group-item']/span)[last()]"))
            .timeout(2000L)
            .visible());

        selenium(seleniumActionBuilder -> seleniumActionBuilder
            .find()
            .element(By.xpath("(//li[@class='list-group-item']/span)[last()]"))
            .text("${todoName}"));
    }
}
