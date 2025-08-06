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

import org.apache.hc.core5.http.ContentType;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class RouteMessagesHttpIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    @Qualifier("bakeryClient")
    private HttpClient bakeryClient;

    @Autowired
    @Qualifier("workerChocolateEndpoint")
    private JmsEndpoint workerChocolateEndpoint;

    @Autowired
    @Qualifier("workerCaramelEndpoint")
    private JmsEndpoint workerCaramelEndpoint;

    @Autowired
    @Qualifier("workerBlueberryEndpoint")
    private JmsEndpoint workerBlueberryEndpoint;

    @CitrusTest
    public void routeMessagesContentBased() {
        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"chocolate\", \"id\": citrus:randomNumber(10), \"amount\": 1}}"));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(receive()
            .endpoint(workerChocolateEndpoint)
            .message()
            .body("<order><type>chocolate</type><id>@ignore@</id><amount>1</amount></order>"));

        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"caramel\", \"id\": citrus:randomNumber(10), \"amount\": 1}}"));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(receive()
            .endpoint(workerCaramelEndpoint)
            .message()
            .body("<order><type>caramel</type><id>@ignore@</id><amount>1</amount></order>"));

        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"order\": { \"type\": \"blueberry\", \"id\": citrus:randomNumber(10), \"amount\": 1}}"));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(receive()
            .endpoint(workerBlueberryEndpoint)
            .message()
            .body("<order><type>blueberry</type><id>@ignore@</id><amount>1</amount></order>"));
    }

    @CitrusTest
    public void routeUnknownOrderType() {
        $(http()
            .client(bakeryClient)
            .send()
            .post("/order")
            .message()
            .body("{ \"order\": { \"type\": \"brownie\", \"id\": citrus:randomNumber(10), \"amount\": 1}}"));

        $(http()
            .client(bakeryClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(receive()
            .endpoint("jms:factory.unknown.inbound")
            .message()
            .body("<order><type>brownie</type><id>@ignore@</id><amount>1</amount></order>"));
    }
}
