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
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.dsl.JsonSupport.json;
import static org.citrusframework.dsl.JsonSupport.marshal;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketApprovalTest {

    @CitrusEndpoint
    HttpClient foodMarketApiClient;

    @CitrusEndpoint
    SeleniumBrowser browser;

    @CitrusResource
    TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldRequireApproval() {
        Product product = new Product("Cherry");

        Supply supply = new Supply("cherry-supplier", product, 200, 1.99D);
        createSupply(supply);

        Booking booking = new Booking("cherry-client", product, 200, 1.99D, TestHelper.createShippingAddress().getFullAddress());
        createBooking(booking);

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.APPROVAL_REQUIRED, dataSource)));

        approveBooking();

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.COMPLETED, dataSource).withRetryAttempts(10)));
    }

    @Test
    void shouldRequireApprovalUi() {
        Product product = new Product("Mango");

        Supply supply = new Supply("mango-supplier", product, 200, 1.99D);
        createSupply(supply);

        Booking booking = new Booking("mango-client", product, 200, 1.99D, TestHelper.createShippingAddress().getFullAddress());
        createBooking(booking);

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.APPROVAL_REQUIRED, dataSource)));

        approveBookingUi();

        t.then(t.applyBehavior(new VerifyBookingStatus(Booking.Status.COMPLETED, dataSource).withRetryAttempts(10)));
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

    /**
     * Approve booking by calling the FoodMarket Http REST API.
     * Uses a test variable bookingId to identify the booking to approve.
     * The bookingId usually gets extracted from the response when creating the booking in an earlier step in this test.
     */
    private void approveBooking() {
        t.when(http()
                .client(foodMarketApiClient)
                .send()
                .put("/api/bookings/approval/${bookingId}")
                .message());

        t.then(http()
                .client(foodMarketApiClient)
                .receive()
                .response(HttpStatus.ACCEPTED));
    }

    /**
     * Approve booking by via FoodMarket UI.
     * Opens new browser with Selenium and makes user click on APPROVE button for the current booking.
     * Uses a test variable bookingId to identify the booking to approve.
     * The bookingId usually gets extracted from the response when creating the booking in an earlier step in this test.
     */
    private void approveBookingUi() {
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
}
