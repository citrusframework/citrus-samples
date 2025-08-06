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
import org.apache.camel.demo.model.Supply;
import org.citrusframework.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.Resources;
import org.citrusframework.variable.dictionary.json.JsonPathMappingDataDictionary;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { CitrusEndpointConfig.class })
class FoodMarketOpenApiTest implements TestActionSupport {

    private final OpenApiSpecification foodMarketSpec =
            OpenApiSpecification.from(Resources.fromClasspath("openapi.yaml"));

    @CitrusEndpoint
    HttpClient foodMarketApiClient;

    @CitrusResource
    TestCaseRunner t;

    @Inject
    DataSource dataSource;

    @Test
    void shouldAddBooking() {
        t.when(openapi()
                .specification(foodMarketSpec)
                .client(foodMarketApiClient)
                .send("addBooking")
                .message()
                .dictionary(getBookingDictionary()));

        t.then(openapi()
                .specification(foodMarketSpec)
                .client(foodMarketApiClient)
                .receive("addBooking", HttpStatus.CREATED));
    }

    @Test
    void shouldAddSupply() {
        t.when(openapi()
                .specification(foodMarketSpec)
                .client(foodMarketApiClient)
                .send("addSupply")
                .message()
                .dictionary(getSupplyDictionary()));

        t.then(openapi()
                .specification(foodMarketSpec)
                .client(foodMarketApiClient)
                .receive("addSupply", HttpStatus.CREATED));
    }

    private JsonPathMappingDataDictionary getBookingDictionary() {
        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();

        dictionary.getMappings().put("$.product.name", "citrus:randomEnumValue('Orange','Strawberry','Banana')");
        dictionary.getMappings().put("$.product.id", "");
        dictionary.getMappings().put("$.amount", "citrus:randomNumber(2)");
        dictionary.getMappings().put("$.price", "0.1citrus:randomNumber(1, true)");
        dictionary.getMappings().put("$.status", Booking.Status.PENDING.name());
        dictionary.setDirection(MessageDirection.OUTBOUND);

        return dictionary;
    }

    private JsonPathMappingDataDictionary getSupplyDictionary() {
        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();

        dictionary.getMappings().put("$.product.name", "citrus:randomEnumValue('Orange','Strawberry','Banana')");
        dictionary.getMappings().put("$.product.id", "");
        dictionary.getMappings().put("$.amount", "citrus:randomNumber(2)");
        dictionary.getMappings().put("$.price", "5.citrus:randomNumber(2, true)");
        dictionary.getMappings().put("$.status", Supply.Status.AVAILABLE.name());
        dictionary.setDirection(MessageDirection.OUTBOUND);

        return dictionary;
    }

}
