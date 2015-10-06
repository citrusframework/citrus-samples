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
public class Reporting_Ok_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void getJsonReport() {
        echo("Receive Json report");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .path("/json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": \"@isNumber()@\",\"bread\": \"@isNumber()@\",\"cake\": \"@isNumber()@\"}");
    }

    @CitrusTest
    public void getHtmlReport() {
        echo("Receive Html report");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET);

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK)
                .payload("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                            "<head>\n" +
                                "<title></title>\n" +
                            "</head>\n" +
                        "<body>\n" +
                            "<h1>Camel bakery reporting</h1>\n" +
                            "<p>Today we have produced following goods:</p>\n" +
                            "<ul>\n" +
                                "<li>@startsWith('cake:')@</li>\n" +
                                "<li>@startsWith('pretzel:')@</li>\n" +
                                "<li>@startsWith('bread:')@</li>\n" +
                            "</ul>" +
                            "<p><a href=\"reporting/orders\">Show orders</a></p>" +
                        "</body>" +
                        "</html>")
                .build().setMessageType("xhtml");
    }

    @CitrusTest
    public void resetReport() {
        echo("Add some 'cake', 'pretzel' and 'bread' orders");

        send(reportingClient)
                .http()
                .method(HttpMethod.PUT)
                .queryParam("id", "citrus:randomNumber(10)")
                .queryParam("name", "cake")
                .queryParam("amount", "10");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        send(reportingClient)
                .http()
                .method(HttpMethod.PUT)
                .queryParam("id", "citrus:randomNumber(10)")
                .queryParam("name", "pretzel")
                .queryParam("amount", "100");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        send(reportingClient)
                .http()
                .method(HttpMethod.PUT)
                .queryParam("id", "citrus:randomNumber(10)")
                .queryParam("name", "bread")
                .queryParam("amount", "5");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        echo("Receive report with changed data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .path("/json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": \"@greaterThan(0)@\",\"bread\": \"@greaterThan(0)@\",\"cake\": \"@greaterThan(0)@\"}");

        echo("Reset report data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .path("/reset");

        receive(reportingClient)
                .http()
                .status(HttpStatus.OK);

        echo("Receive empty report data");

        send(reportingClient)
                .http()
                .method(HttpMethod.GET)
                .path("/json");

        receive(reportingClient)
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK)
                .payload("{\"pretzel\": 0,\"bread\": 0,\"cake\": 0}");
    }
}
