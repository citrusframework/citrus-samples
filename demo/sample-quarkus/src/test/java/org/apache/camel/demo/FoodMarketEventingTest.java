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
import org.apache.camel.demo.behavior.GetShippingAddress;
import org.apache.camel.demo.behavior.VerifyBookingCompletedMail;
import org.apache.camel.demo.behavior.WaitForEntityPersisted;
import org.apache.camel.demo.behavior.WaitForProductCreated;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.apache.camel.demo.model.ShippingAddress;
import org.apache.camel.demo.model.Supply;
import org.apache.camel.demo.model.event.BookingCompletedEvent;
import org.apache.camel.demo.model.event.ShippingEvent;
import org.citrusframework.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

import static org.citrusframework.dsl.JsonSupport.marshal;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketEventingTest implements TestActionSupport {

    @CitrusEndpoint
    KafkaEndpoint products;

    @CitrusEndpoint
    KafkaEndpoint bookings;

    @CitrusEndpoint
    KafkaEndpoint supplies;

    @CitrusEndpoint
    KafkaEndpoint completed;

    @CitrusEndpoint
    KafkaEndpoint shipping;

    @CitrusEndpoint
    MailServer mailServer;

    @CitrusEndpoint
    HttpServer shippingDetailsService;

    @CitrusResource
    TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldCompleteOnBooking() {
        Product product = new Product("Pineapple");

        Supply supply = new Supply("pineapple-supplier", product, 100, 0.90D);
        createSupply(supply);

        t.then(t.applyBehavior(new WaitForEntityPersisted(supply, dataSource)));

        Booking booking = new Booking("pineapple-client", product, 100, 0.99D);
        createBooking(booking);

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        verifyBookingCompletedEvent(completedEvent);

        ShippingAddress shippingAddress = TestHelper.createShippingAddress();
        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(),
                supply.getAmount(), shippingAddress.getFullAddress());

        t.then(t.applyBehavior(new GetShippingAddress(shippingDetailsService, shippingAddress)));

        verifyShippingEvent(shippingEvent);

        t.then(t.applyBehavior(new VerifyBookingCompletedMail(booking, mailServer)));
    }

    @Test
    void shouldCompleteOnSupply() {
        Product product = new Product("Watermelon");
        createProduct(product);

        t.then(t.applyBehavior(new WaitForProductCreated(product, dataSource)));

        Booking booking = new Booking("watermelon-client", product, 100, 0.99D);
        createBooking(booking);

        t.then(t.applyBehavior(new WaitForEntityPersisted(booking, dataSource)));

        Supply supply = new Supply("watermelon-supplier", product, 100, 0.99D);
        createSupply(supply);

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        verifyBookingCompletedEvent(completedEvent);

        ShippingAddress shippingAddress = TestHelper.createShippingAddress();
        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(),
                supply.getAmount(), shippingAddress.getFullAddress());

        t.then(t.applyBehavior(new GetShippingAddress(shippingDetailsService, shippingAddress)));

        verifyShippingEvent(shippingEvent);

        t.then(t.applyBehavior(new VerifyBookingCompletedMail(booking, mailServer)));
    }

    @Test
    void shouldCompleteAllMatchingBookings() {
        Product product = new Product("Apple");
        t.variable("product", product);

        Booking booking = new Booking("apple-client", product, 10, 1.99D, TestHelper.createShippingAddress().getFullAddress());
        t.when(iterate()
                .condition((i, context) -> i < 10)
                .actions(
                    send()
                        .endpoint(bookings)
                        .message().body(marshal(booking))));
        t.variable("booking", booking);

        t.$(delay().milliseconds(1000L));

        Supply supply = new Supply("apple-supplier", product, 100, 0.99D);
        t.then(send()
                .endpoint(supplies)
                .message().body(marshal(supply)));

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(),
                booking.getAmount(), booking.getShippingAddress());

        t.then(parallel().actions(
                iterate()
                    .condition((i, context) -> i < 10)
                    .actions(
                        receive()
                            .endpoint(completed)
                            .message().body(marshal(completedEvent))
                    ),
                iterate()
                    .condition((i, context) -> i < 10)
                    .actions(
                        receive()
                            .endpoint(shipping)
                            .message().body(marshal(shippingEvent))
                    )
        ));
    }

    private void verifyShippingEvent(ShippingEvent shippingEvent) {
        t.then(receive()
                .endpoint(shipping)
                .message().body(marshal(shippingEvent)));
    }

    private void verifyBookingCompletedEvent(BookingCompletedEvent completedEvent) {
        t.then(receive()
                .endpoint(completed)
                .message().body(marshal(completedEvent)));
    }

    private void createSupply(Supply supply) {
        t.when(send()
                .endpoint(supplies)
                .message().body(marshal(supply)));
    }

    private void createBooking(Booking booking) {
        t.variable("booking", booking);
        t.when(send()
                .endpoint(bookings)
                .message().body(marshal(booking)));
    }

    private void createProduct(Product product) {
        t.when(send()
                .endpoint(products)
                .message().body(marshal(product)));
    }

}
