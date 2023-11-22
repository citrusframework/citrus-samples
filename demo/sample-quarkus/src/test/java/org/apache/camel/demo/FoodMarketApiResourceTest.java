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
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.dsl.JsonSupport.json;
import static org.citrusframework.dsl.JsonSupport.marshal;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketApiResourceTest {

    @CitrusEndpoint
    private HttpClient httpClient;

    @CitrusResource
    private TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldAddBooking() {
        Product product = new Product("Kiwi");
        Booking booking = new Booking("citrus-test", product, 100, 0.99D);
        t.when(http()
                .client(httpClient)
                .send()
                .post("/bookings")
                .message()
                .contentType(APPLICATION_JSON)
                .body(marshal(booking)));

        t.then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .extract(json().expression("$.id", "${bookingId}")));

        t.then(sql()
                .dataSource(dataSource)
                .query()
                .statement("select status from booking where booking.id=${bookingId}")
                .validate("status", "PENDING"));
    }

    @Test
    void shouldAddSupply() {
        Product product = new Product("Cherry");
        Supply supply = new Supply("citrus-test", product, 100, 0.99D);
        t.when(http()
                .client(httpClient)
                .send()
                .post("/supplies")
                .message()
                .contentType(APPLICATION_JSON)
                .body(marshal(supply)));

        t.then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .extract(json().expression("$.id", "${supplyId}")));


        t.then(sql()
                .dataSource(dataSource)
                .query()
                .statement("select status from supply where supply.id=${supplyId}")
                .validate("status", "AVAILABLE"));
    }

}
