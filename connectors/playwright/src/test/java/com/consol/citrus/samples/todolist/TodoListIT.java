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
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.playwright.actions.PlaywrightActionBuilder.playwright;


/**
 * UI tests for the todo-list app using raw Playwright locators (no page objects).
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private PlaywrightBrowser browser;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testIndexPage() {
        $(playwright()
                .browser(browser)
                .start());

        $(playwright()
                .browser(browser)
                .open()
                .url(todoClient.getEndpointConfiguration().getRequestUrl()));

        $(playwright()
                .browser(browser)
                .verify()
                .locator(LocatorSpec.text("Run application"))
                .visible()
                .enabled());

        $(playwright()
                .browser(browser)
                .click()
                .locator(LocatorSpec.text("Run application")));

        $(playwright()
                .browser(browser)
                .verify()
                .locator("h1")
                .text("TODO list"));
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

        $(playwright()
            .browser(browser)
            .start());

        $(playwright()
            .browser(browser)
            .open()
            .url(todoClient.getEndpointConfiguration().getRequestUrl() + "/todolist"));

        $(playwright()
            .browser(browser)
            .verify()
            .locator(LocatorSpec.text("No todos found"))
            .text("No todos found"));

        $(playwright()
            .browser(browser)
            .fill()
            .locator("input[name=title]")
            .value("${todoName}"));

        $(playwright()
            .browser(browser)
            .fill()
            .locator("textarea[name=description]")
            .value("${todoDescription}"));

        $(playwright()
            .browser(browser)
            .click()
            .locator("form button[type=submit]"));

        $(playwright()
            .browser(browser)
            .waitFor()
            .locator(LocatorSpec.text("${todoName}"))
            .visible());

        $(playwright()
            .browser(browser)
            .verify()
            .locator(LocatorSpec.text("${todoName}"))
            .text("${todoName}"));
    }
}
