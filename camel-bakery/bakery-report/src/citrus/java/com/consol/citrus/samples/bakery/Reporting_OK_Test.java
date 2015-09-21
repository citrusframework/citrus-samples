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
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
@Test
public class Reporting_OK_Test extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void addOrders() {
        echo("First receive empty report");

        send(reportingClient)
            .http()
                .method(HttpMethod.GET)
                .queryParam("type", "json");

        receive(reportingClient)
            .messageType(MessageType.JSON)
            .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": 0,\"bread\": 0,\"cake\": 0}");

        echo("Add some 'cake' orders");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("name", "cake")
                .queryParam("amount", "10");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        echo("Receive report with changed data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("type", "json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": 0,\"bread\": 0,\"cake\": 10}");
    }

    @CitrusTest
    public void resetReport() {
        echo("Add some 'cake', 'pretzel' and 'bread' orders");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("name", "cake")
                .queryParam("amount", "10");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("name", "pretzel")
                .queryParam("amount", "100");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("name", "bread")
                .queryParam("amount", "5");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        echo("Receive report with changed data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("type", "json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": \"@greaterThan(0)@\",\"bread\": \"@greaterThan(0)@\",\"cake\": \"@greaterThan(0)@\"}");

        echo("Reset report data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("reset", "true");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        echo("Receive empty report data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .queryParam("type", "json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": 0,\"bread\": 0,\"cake\": 0}");
    }
}
