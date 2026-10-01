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

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
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
 * Composes todo-list UI tests from reusable test behaviors. Shows Citrus 5.1.0
 * behavior features: named reporting, andThen composition, requires guards,
 * and isolated execution with published variables.
 */
public class TodoBehaviorsIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private PlaywrightBrowser browser;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testAddEntry() {
        // Isolated behavior computes the entry name without polluting this test,
        // then publishes the variables back to the calling test.
        $(apply().behavior(generateTodoEntryName())
            .isolated()
            .publish("todoName", "todoDescription"));

        // requires() fails fast if a previous step forgot to set these.
        $(apply().behavior(clearTodoList()
                .andThen(openTodoList())
                .andThen(addTodoEntry()))
            .requires("todoName", "todoDescription"));

        $(applyBehavior(verifyTodoEntry()));
    }

    @Test
    @CitrusTest
    public void testAddEntryWithLambda() {
        variable("todoName", "todo_citrus:randomNumber(4)");

        // Lambdas get the generic report name "behavior", so name them explicitly.
        $(applyBehavior(TestBehavior.named("clear todo backend", runner ->
            runner.$(http()
                .client(todoClient)
                .send()
                .delete("/api/todolist")))));

        // Override defaults per call, no new class needed.
        $(applyBehavior(openTodoList()
            .andThen(addTodoEntry()
                .withTitle("${todoName}")
                .withDescription("Urgent: ${todoName}"))));
    }

    /** Factory methods — the fluent DSL entry points. */
    private GenerateTodoEntryNameBehavior generateTodoEntryName() {
        return new GenerateTodoEntryNameBehavior();
    }

    private ClearTodoListBehavior clearTodoList() {
        return new ClearTodoListBehavior();
    }

    private OpenTodoListBehavior openTodoList() {
        return new OpenTodoListBehavior();
    }

    private AddTodoEntryBehavior addTodoEntry() {
        return new AddTodoEntryBehavior();
    }

    private VerifyTodoEntryBehavior verifyTodoEntry() {
        return new VerifyTodoEntryBehavior();
    }

    /** Sets todoName/todoDescription. Runs isolated, caller keeps only published vars. */
    private class GenerateTodoEntryNameBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            runner.$(createVariables()
                .variable("todoName", "todo_citrus:randomNumber(4)")
                .variable("todoDescription", "Description: ${todoName}"));
        }
    }

    /** Deletes all entries via REST. Reported as "clear todo list". */
    private class ClearTodoListBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            runner.$(http()
                .client(todoClient)
                .send()
                .delete("/api/todolist"));

            runner.$(http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK));
        }
    }

    /** Starts the shared browser and opens the todo list. Reported as "open todo list". */
    private class OpenTodoListBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            runner.$(playwright()
                .browser(browser)
                .start());

            runner.$(playwright()
                .browser(browser)
                .open()
                .url(todoClient.getEndpointConfiguration().getRequestUrl() + "/todolist"));
        }
    }

    /** Fills and submits the entry form, waits until the entry shows. */
    private class AddTodoEntryBehavior implements TestBehavior {

        private String title = "${todoName}";
        private String description = "${todoDescription}";

        AddTodoEntryBehavior withTitle(String title) {
            this.title = title;
            return this;
        }

        AddTodoEntryBehavior withDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.$(playwright()
                .browser(browser)
                .fill()
                .locator("input[name=title]")
                .value(title));

            runner.$(playwright()
                .browser(browser)
                .fill()
                .locator("textarea[name=description]")
                .value(description));

            runner.$(playwright()
                .browser(browser)
                .click()
                .locator("form button[type=submit]"));

            runner.$(playwright()
                .browser(browser)
                .waitFor()
                .locator(LocatorSpec.text(title))
                .visible());
        }
    }

    /** Verifies the new entry text on the page. */
    private class VerifyTodoEntryBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            runner.$(playwright()
                .browser(browser)
                .verify()
                .locator(LocatorSpec.text("${todoName}"))
                .text("${todoName}"));
        }
    }
}
