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

import java.util.Map;

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.Functions;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.AbstractValidationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class ReportOrdersIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void getOrders() {
        final String orderId = Functions.randomNumber(10L, null);
        variable("orderId", orderId);

        $(echo("First check order id not present"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/orders"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .validate(new AbstractValidationProcessor<String>() {
                @Override
                public void validate(String body, Map headers, TestContext context) {
                    Assert.assertFalse(body.contains(orderId));
                }
            }));

        $(echo("Add some 'blueberry' order with id"));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "${orderId}")
            .queryParam("name", "blueberry")
            .queryParam("amount", "1"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(echo("Receive order id in list of produced goods"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/orders"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .validate(new AbstractValidationProcessor<String>() {
                @Override
                public void validate(String body, Map headers, TestContext context) {
                    Assert.assertTrue(body.contains(orderId));
                }
            }));
    }

    @CitrusTest
    public void addOrders() {
        $(echo("First receive report and save current amount of produced chocolate cookies to variable"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"caramel\": \"@ignore@\",\"blueberry\": \"@ignore@\",\"chocolate\": \"@variable('producedCookies')@\"}"));

        $(echo("Add some 'chocolate' orders"));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "citrus:randomNumber(10)")
            .queryParam("name", "chocolate")
            .queryParam("amount", "10"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(echo("Receive report with changed data"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"caramel\": \"@ignore@\",\"blueberry\": \"@ignore@\",\"chocolate\": \"@greaterThan(${producedCookies})@\"}"));
    }

    @CitrusTest
    public void getOrderStatus() {
        variable("orderId", Functions.randomNumber(10L, null));

        $(echo("First receive negative order status"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/order")
            .queryParam("id", "${orderId}"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"status\": false}"));

        $(echo("Add some 'caramel' order with id"));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "${orderId}")
            .queryParam("name", "caramel")
            .queryParam("amount", "1"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(echo("Receive report positive status for order id"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/order")
            .queryParam("id", "${orderId}"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"status\": true}"));
    }
}
