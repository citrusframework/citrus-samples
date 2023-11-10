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
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.apache.camel.demo.model.Supply;
import org.apache.camel.demo.model.event.BookingCompletedEvent;
import org.apache.camel.demo.model.event.ShippingEvent;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.actions.SleepAction.Builder.delay;
import static org.citrusframework.container.Iterate.Builder.iterate;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static org.citrusframework.dsl.JsonSupport.marshal;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketApplicationTest {

    @CitrusEndpoint
    private KafkaEndpoint products;

    @CitrusEndpoint
    private KafkaEndpoint bookings;

    @CitrusEndpoint
    private KafkaEndpoint supplies;

    @CitrusEndpoint
    private KafkaEndpoint completed;

    @CitrusEndpoint
    private KafkaEndpoint shipping;

    @CitrusEndpoint
    private MailServer mailServer;

    @CitrusResource
    private TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldCompleteOnSupply() {
        Product product = new Product("Watermelon");
        t.when(send()
            .endpoint(products)
            .message().body(marshal(product)));

        t.then(repeatOnError()
            .condition((i, context) -> i > 25)
            .autoSleep(500)
            .actions(sql().dataSource(dataSource)
                    .query()
                    .statement("select count(id) as found from product where product.name='%s'".formatted(product.getName()))
                    .validate("found", "1"))
        );

        Booking booking = new Booking("citrus-test", product, 100, 0.99D);
        t.when(send()
                .endpoint(bookings)
                .message().body(marshal(booking)));

        t.then(repeatOnError()
            .condition((i, context) -> i > 25)
            .autoSleep(500)
            .actions(sql().dataSource(dataSource)
                    .query()
                    .statement("select count(id) as found from booking where booking.status='PENDING'")
                    .validate("found", "1"))
        );

        Supply supply = new Supply(product, 100, 0.99D);
        t.when(send()
                .endpoint(supplies)
                .message().body(marshal(supply)));

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        completedEvent.setStatus(Booking.Status.COMPLETED.name());

        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(), supply.getAmount(), "@ignore@");
        t.then(parallel().actions(
            receive()
                .endpoint(completed)
                .message().body(marshal(completedEvent)),
            receive()
                .endpoint(shipping)
                .message().body(marshal(shippingEvent))
        ));

        t.then(receive()
            .endpoint(mailServer)
            .message(MailMessage.request("foodmarket@quarkus.io", "%s@quarkus.io".formatted(completedEvent.getClient()), "Booking completed!")
                .body("Hey %s, your booking %s has been completed.".formatted(completedEvent.getClient(), completedEvent.getProduct()), "text/plain"))
        );

        t.then(send()
            .endpoint(mailServer)
            .message(MailMessage.response())
        );
    }

    @Test
    void shouldCompleteOnBooking() {
        Product product = new Product("Pineapple");

        Supply supply = new Supply(product, 100, 0.90D);
        t.when(send()
            .endpoint(supplies)
            .message().body(marshal(supply)));

        t.then(repeatOnError()
            .condition((i, context) -> i > 25)
            .autoSleep(500)
            .actions(sql().dataSource(dataSource)
                    .query()
                    .statement("select count(id) as found from supply where supply.status='AVAILABLE'")
                    .validate("found", "1"))
        );

        Booking booking = new Booking("citrus-test", product, 100, 0.99D);
        t.when(send()
            .endpoint(bookings)
            .message().body(marshal(booking)));

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        completedEvent.setStatus(Booking.Status.COMPLETED.name());

        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(), supply.getAmount(), "@ignore@");
        t.then(parallel().actions(
            receive()
                .endpoint(completed)
                .message().body(marshal(completedEvent)),
            receive()
                .endpoint(shipping)
                .message().body(marshal(shippingEvent))
        ));

        t.then(receive()
            .endpoint(mailServer)
            .message(MailMessage.request("foodmarket@quarkus.io", "%s@quarkus.io".formatted(completedEvent.getClient()), "Booking completed!")
                .body("Hey %s, your booking %s has been completed.".formatted(completedEvent.getClient(), completedEvent.getProduct()), "text/plain"))
        );

        t.then(send()
            .endpoint(mailServer)
            .message(MailMessage.response())
        );
    }

    @Test
    void shouldCompleteAllMatchingBookings() {
        Product product = new Product("Apple");
        t.variable("product", product);

        Booking booking = new Booking("citrus-test", product, 10, 1.99D);
        t.when(iterate()
                .condition((i, context) -> i < 10)
                .actions(
                        send()
                            .endpoint(bookings)
                            .message().body(marshal(booking))));
        t.variable("booking", booking);

        t.$(delay().milliseconds(1000L));

        Supply supply = new Supply(product, 100, 0.99D);

        BookingCompletedEvent completedEvent = BookingCompletedEvent.from(booking);
        completedEvent.setStatus(Booking.Status.COMPLETED.name());

        ShippingEvent shippingEvent = new ShippingEvent(booking.getClient(), product.getName(), booking.getAmount(), "@ignore@");

        t.then(parallel().actions(
            send()
                .endpoint(supplies)
                .message().body(marshal(supply)),
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
}
