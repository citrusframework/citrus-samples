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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class ReportSummaryIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @CitrusTest
    public void getJsonReport() {
        $(echo("Receive Json report"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"caramel\": \"@isNumber()@\",\"blueberry\": \"@isNumber()@\",\"chocolate\": \"@isNumber()@\"}"));
    }

    @CitrusTest
    public void getHtmlReport() {
        $(echo("Receive Html report"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">\n" +
                     "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                         "<head>\n" +
                             "<title></title>\n" +
                         "</head>\n" +
                         "<body>\n" +
                             "<h1>Camel bakery reporting</h1>\n" +
                             "<p>Today we have produced following goods:</p>\n" +
                             "<ul>\n" +
                                 "<li>@startsWith('chocolate:')@</li>\n" +
                                 "<li>@startsWith('caramel:')@</li>\n" +
                                 "<li>@startsWith('blueberry:')@</li>\n" +
                             "</ul>" +
                             "<p><a href=\"reporting/orders\">Show orders</a></p>" +
                         "</body>" +
                     "</html>")
                .build());
    }

    @CitrusTest
    public void resetReport() {
        $(echo("Add some 'chocolate', 'caramel' and 'blueberry' orders"));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "citrus:randomNumber(10)")
            .queryParam("name", "chocolate")
            .queryParam("amount", "10"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "citrus:randomNumber(10)")
            .queryParam("name", "caramel")
            .queryParam("amount", "100"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(http()
            .client(reportingClient)
            .send()
            .put("/reporting")
            .queryParam("id", "citrus:randomNumber(10)")
            .queryParam("name", "blueberry")
            .queryParam("amount", "5"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(echo("Receive report with changed data"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"caramel\": \"@greaterThan(0)@\",\"blueberry\": \"@greaterThan(0)@\",\"chocolate\": \"@greaterThan(0)@\"}"));

        $(echo("Reset report data"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/reset"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        $(echo("Receive empty report data"));

        $(http()
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        $(http()
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{\"caramel\": 0,\"blueberry\": 0,\"chocolate\": 0}"));
    }
}
