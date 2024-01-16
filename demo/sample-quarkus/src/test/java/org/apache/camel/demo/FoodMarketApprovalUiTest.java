/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.demo;

import javax.sql.DataSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.demo.behavior.VerifyBookingStatus;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.apache.camel.demo.model.Supply;
import org.apache.camel.demo.model.event.BookingCompletedEvent;
import org.apache.camel.demo.model.event.ShippingEvent;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.dsl.JsonSupport.json;
import static org.citrusframework.dsl.JsonSupport.marshal;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketApprovalUiTest {

    @CitrusEndpoint
    HttpClient foodMarketApiClient;

    @CitrusEndpoint
    KafkaEndpoint completed;

    @CitrusEndpoint
    KafkaEndpoint shipping;

    @CitrusEndpoint
    SeleniumBrowser browser;

    @CitrusResource
    TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldRequireApproval() {
        Product product = new Product("Kiwi");

        Supply supply = new Supply("citrus-test", product, 200, 1.99D);
        createSupply(supply);

        Booking booking = new Booking("citrus-test", product, 200, 1.99D, TestHelper.createShippingAddress().getFullAddress());
        createBooking(booking);

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.APPROVAL_REQUIRED, dataSource)));

        approveBooking();

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        t.then(receive()
                .endpoint(completed)
                .message().body(marshal(completedEvent)));

        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(),
                supply.getAmount(), booking.getShippingAddress());

        t.then(receive()
                .endpoint(shipping)
                .message().body(marshal(shippingEvent)));

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.COMPLETED, dataSource)));
    }

    /**
     * Approve booking by via FoodMarket UI.
     * Opens new browser with Selenium and makes user click on APPROVE button for the current booking.
     * Uses a test variable bookingId to identify the booking to approve.
     * The bookingId usually gets extracted from the response when creating the booking in an earlier step in this test.
     */
    private void approveBooking() {
        String homeUrl = "http://localhost:8081";

        t.given(selenium()
                .browser(browser)
                .start());

        t.given(doFinally().actions(
                selenium()
                        .browser(browser)
                        .stop()));

        t.when(selenium()
                .browser(browser)
                .navigate(homeUrl));

        t.then(selenium()
                .browser(browser)
                .find()
                .element("id", "${bookingId}"));

        t.then(selenium()
                .browser(browser)
                .click()
                .element("id", "${bookingId}"));
    }

    private void createBooking(Booking booking) {
        t.variable("booking", booking);
        t.when(http()
                .client(foodMarketApiClient)
                .send()
                .post("/api/bookings")
                .message()
                .contentType(APPLICATION_JSON)
                .body(marshal(booking)));

        t.then(http()
                .client(foodMarketApiClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .extract(json().expression("$.id", "bookingId")));
    }

    private void createSupply(Supply supply) {
        t.when(http()
                .client(foodMarketApiClient)
                .send()
                .post("/api/supplies")
                .message()
                .contentType(APPLICATION_JSON)
                .body(marshal(supply)));

        t.then(http()
                .client(foodMarketApiClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .extract(json().expression("$.id", "supplyId")));
    }

}
