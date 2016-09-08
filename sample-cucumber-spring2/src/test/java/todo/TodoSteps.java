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

package todo;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import cucumber.api.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class TodoSteps {

    /** Test designer filled with actions by step definitions */
    @CitrusResource
    private TestDesigner designer;

    @Autowired
    private HttpClient todoListClient;

    @Given("^Todo list is empty$")
    public void empty_todos() {
        designer.http()
                .client(todoListClient)
                .delete("/todolist");

        designer.http()
                .client(todoListClient)
                .response(HttpStatus.FOUND);
    }

    @When("^I add entry \"([^\"]*)\"$")
    public void add_entry(String todoName) {
        designer.http()
                .client(todoListClient)
                .post("/todolist")
                .contentType("application/x-www-form-urlencoded")
                .payload("title=" + todoName);

        designer.http()
                .client(todoListClient)
                .response(HttpStatus.FOUND);
    }

    @When("^I remove entry \"([^\"]*)\"$")
    public void remove_entry(String todoName) {
        designer.http()
                .client(todoListClient)
                .delete("/todo?title=" + todoName);

        designer.http()
                .client(todoListClient)
                .response(HttpStatus.FOUND)
                .messageType(MessageType.PLAINTEXT);
    }

    @Then("^the number of todo entries should be (\\d+)$")
    public void verify_todos(int todoCnt) {
        designer.http()
                .client(todoListClient)
                .get("/todolist/count");

        designer.http()
                .client(todoListClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload(String.valueOf(todoCnt));
    }

    @Then("^the todo list should be empty$")
    public void verify_empty_todos() {
        verify_todos(0);
    }

}
