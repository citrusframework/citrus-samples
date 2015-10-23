/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.samples.bakery;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class BakeryWeb_Http_Ok_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("bakeryClient")
    private HttpClient bakeryClient;

    @Autowired
    @Qualifier("workerCakeEndpoint")
    private JmsEndpoint workerCakeEndpoint;

    @Autowired
    @Qualifier("workerPretzelEndpoint")
    private JmsEndpoint workerPretzelEndpoint;

    @Autowired
    @Qualifier("workerBreadEndpoint")
    private JmsEndpoint workerBreadEndpoint;

    @CitrusTest
    public void routeMessagesContentBased() {
        http().client(bakeryClient)
                .post("/order")
                .contentType("application/json")
                .payload("{ \"order\": { \"type\": \"cake\", \"id\": citrus:randomNumber(10), \"amount\": 1}}");

        http().client(bakeryClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT);

        receive(workerCakeEndpoint)
                .payload("<order type=\"cake\"><id>@ignore@</id><amount>1</amount></order>");

        http().client(bakeryClient)
                .post("/order")
                .contentType("application/json")
                .payload("{ \"order\": { \"type\": \"pretzel\", \"id\": citrus:randomNumber(10), \"amount\": 1}}");

        http().client(bakeryClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT);

        receive(workerPretzelEndpoint)
                .payload("<order type=\"pretzel\"><id>@ignore@</id><amount>1</amount></order>");

        http().client(bakeryClient)
                .post("/order")
                .contentType("application/json")
                .payload("{ \"order\": { \"type\": \"bread\", \"id\": citrus:randomNumber(10), \"amount\": 1}}");

        http().client(bakeryClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT);

        receive(workerBreadEndpoint)
                .payload("<order type=\"bread\"><id>@ignore@</id><amount>1</amount></order>");
    }

    @CitrusTest
    public void routeUnknownOrderType() {
        http().client(bakeryClient)
                .post("/order")
                .payload("{ \"order\": { \"type\": \"baguette\", \"id\": citrus:randomNumber(10), \"amount\": 1}}");

        http().client(bakeryClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT);

        receive("jms:factory.unknown.inbound")
                .payload("<order type=\"baguette\"><id>@ignore@</id><amount>1</amount></order>");
    }
}
