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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class TodoSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @Autowired
    private HttpClient todoListClient;

    @Given("^Todo list is empty$")
    public void empty_todos() {
        runner.given(http()
                .client(todoListClient)
                .send()
                .delete("/api/todolist"));

        runner.then(http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.OK));
    }

    @When("^(?:I|user) adds? entry \"([^\"]*)\"$")
    public void add_entry(String todoName) {
        runner.when(http()
                .client(todoListClient)
                .send()
                .post("/todolist")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .payload("title=" + todoName));

        runner.then(http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.FOUND));
    }

    @When("^(?:I|user) removes? entry \"([^\"]*)\"$")
    public void remove_entry(String todoName) throws UnsupportedEncodingException {
        runner.when(http()
                .client(todoListClient)
                .send()
                .delete("/api/todo?title=" + URLEncoder.encode(todoName, "UTF-8")));

        runner.then(http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT));
    }

    @Then("^(?:the )?number of todo entries should be (\\d+)$")
    public void verify_todos(int todoCnt) {
        runner.then(http()
                .client(todoListClient)
                .send()
                .get("/api/todolist/count"));

        runner.and(http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload(String.valueOf(todoCnt)));
    }

    @Then("^(?:the )?todo list should be empty$")
    public void verify_empty_todos() {
        verify_todos(0);
    }

}
