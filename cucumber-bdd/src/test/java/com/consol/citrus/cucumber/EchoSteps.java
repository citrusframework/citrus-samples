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

package com.consol.citrus.cucumber;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.message.MessageType;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;

/**
 * @author Christoph Deppisch
 */
public class EchoSteps {

    /** Test designer filled with actions by step definitions */
    private TestDesigner designer;

    @CitrusFramework
    private Citrus citrus;

    @Before
    public void init(Scenario scenario) {
        designer = new DefaultTestDesigner();
        designer.name(scenario.getId());
        designer.description(scenario.getName());
    }

    @After
    public void execute(Scenario scenario) {
        if (!scenario.isFailed()) {
            citrus.run(designer.getTestCase());
        }
    }

    @Given("^My name is (.*)$")
    public void my_name_is(String name) {
        designer.variable("username", name);
    }

    @When("^I say hello.*$")
    public void say_hello() {
        designer.send("echoEndpoint")
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello, my name is ${username}!");
    }

    @When("^I say goodbye.*$")
    public void say_goodbye() {
        designer.send("echoEndpoint")
            .messageType(MessageType.PLAINTEXT)
            .payload("Goodbye from ${username}!");
    }

    @Then("^the service should return: \"([^\"]*)\"$")
    public void verify_return(final String body) {
        designer.receive("echoEndpoint")
            .messageType(MessageType.PLAINTEXT)
            .payload("You just said: " + body);
    }

}
