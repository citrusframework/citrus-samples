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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.functions.Functions;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.callback.AbstractValidationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class Reporting_Orders_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void getOrders() {
        final String orderId = Functions.randomNumber(10L);
        variable("orderId", orderId);

        echo("First check order id not present");

        http().client(reportingClient)
                .get("/reporting/orders");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .validationCallback(new AbstractValidationCallback<String>() {
                    @Override
                    public void validate(String payload, Map headers, TestContext context) {
                        Assert.assertFalse(payload.contains(orderId));
                    }
                });

        echo("Add some 'bread' order with id");

        http().client(reportingClient)
                .put("/reporting")
                .queryParam("id", "${orderId}")
                .queryParam("name", "bread")
                .queryParam("amount", "1");

        http().client(reportingClient)
                .response(HttpStatus.OK);

        echo("Receive order id in list of produced goods");

        http().client(reportingClient)
                .get("/reporting/orders");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .validationCallback(new AbstractValidationCallback<String>() {
                    @Override
                    public void validate(String payload, Map headers, TestContext context) {
                        Assert.assertTrue(payload.contains(orderId));
                    }
                });
    }

    @CitrusTest
    public void addOrders() {
        echo("First receive report and save current amount of produced cakes to variable");

        http().client(reportingClient)
                .get("/reporting/json");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .payload("{\"pretzel\": \"@ignore@\",\"bread\": \"@ignore@\",\"cake\": \"@variable('producedCakes')@\"}");

        echo("Add some 'cake' orders");

        http().client(reportingClient)
                .put("/reporting")
                .queryParam("id", "citrus:randomNumber(10)")
                .queryParam("name", "cake")
                .queryParam("amount", "10");

        http().client(reportingClient)
                .response(HttpStatus.OK);

        echo("Receive report with changed data");

        http().client(reportingClient)
                .get("/reporting/json");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .payload("{\"pretzel\": \"@ignore@\",\"bread\": \"@ignore@\",\"cake\": \"@greaterThan(${producedCakes})@\"}");
    }

    @CitrusTest
    public void getOrderStatus() {
        variable("orderId", Functions.randomNumber(10L));

        echo("First receive negative order status");

        http().client(reportingClient)
                .get("/reporting/order")
                .queryParam("id", "${orderId}");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("false");

        echo("Add some 'pretzel' order with id");

        http().client(reportingClient)
                .put("/reporting")
                .queryParam("id", "${orderId}")
                .queryParam("name", "pretzel")
                .queryParam("amount", "1");

        http().client(reportingClient)
                .response(HttpStatus.OK);

        echo("Receive report positive status for order id");

        http().client(reportingClient)
                .get("/reporting/order")
                .queryParam("id", "${orderId}");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("true");
    }
}
