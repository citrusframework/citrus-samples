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

import io.quarkus.test.junit.QuarkusTest;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketUiTest {

    @CitrusEndpoint
    SeleniumBrowser browser;

    @CitrusResource
    TestCaseRunner t;

    private final String homeUrl = "http://localhost:8081";

    @Test
    void indexPage() {
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
                .element(By.tagName("h1"))
                .text("Food Market demo"));
    }

    @Test
    void shouldCreateBooking() {
        Product product = new Product("Peach");
        Booking booking = new Booking("browser-client", product, 50, 0.99D, TestHelper.createShippingAddress().getFullAddress());

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
                .select("booking")
                .element(By.id("type")));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("name"))
                .value(booking.getClient()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("product"))
                .value(booking.getProduct().getName()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("amount"))
                .value(booking.getAmount().toString()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("price"))
                .value(booking.getPrice().toString()));

        t.then(selenium()
                .browser(browser)
                .click()
                .element(By.id("save")));
    }

    @Test
    void shouldCreateSupply() {
        Product product = new Product("Peach");
        Booking booking = new Booking("browser-client", product, 50, 0.99D, TestHelper.createShippingAddress().getFullAddress());

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
                .select("supply")
                .element(By.id("type")));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("name"))
                .value(booking.getClient()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("product"))
                .value(booking.getProduct().getName()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("amount"))
                .value(booking.getAmount().toString()));

        t.then(selenium()
                .browser(browser)
                .setInput()
                .element(By.id("price"))
                .value(booking.getPrice().toString()));

        t.then(selenium()
                .browser(browser)
                .click()
                .element(By.id("save")));
    }

}
