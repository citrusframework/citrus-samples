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
import org.citrusframework.functions.Functions;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.mail.message.CitrusMailMessageHeaders;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class PlaceBulkOrderIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("bakeryOrderEndpoint")
    private JmsEndpoint bakeryOrderEndpoint;

    @Autowired
    @Qualifier("reportingClient")
    private HttpClient reportingClient;

    @Autowired
    @Qualifier("mailServer")
    private MailServer mailServer;

    @CitrusTest
    public void placeBulkCookieOrder() {
        $(echo("Add 1000+ order and receive mail"));

        variable("orderType", "chocolate");
        variable("orderId", Functions.randomNumber(10L, null));
        variable("amount", 1001L);

        $(sleep().milliseconds(5000L));

        $(send()
            .endpoint(bakeryOrderEndpoint)
            .message()
            .body("<order><type>${orderType}</type><id>${orderId}</id><amount>${amount}</amount></order>"));

        $(echo("Receive report mail for 1000+ order"));

        $(receive()
            .endpoint(mailServer)
            .message()
            .body(Resources.fromClasspath("templates/mail.xml"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Congratulations!")
            .header(CitrusMailMessageHeaders.MAIL_FROM, "cookie-report@example.com")
            .header(CitrusMailMessageHeaders.MAIL_TO, "stakeholders@example.com"));

        $(send()
            .endpoint(mailServer)
            .message()
            .body(Resources.fromClasspath("templates/mail_response.xml")));

        $(echo("Receive report with 1000+ order"));

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
            .body("{\"caramel\": \"@ignore@\",\"blueberry\": \"@ignore@\",\"chocolate\": \"@greaterThan(1000)@\"}"));
    }
}
