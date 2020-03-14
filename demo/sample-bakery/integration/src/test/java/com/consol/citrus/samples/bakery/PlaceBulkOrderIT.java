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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.Functions;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class PlaceBulkOrderIT extends TestNGCitrusTestRunner {

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
        echo("Add 1000+ order and receive mail");

        variable("orderType", "chocolate");
        variable("orderId", Functions.randomNumber(10L, null));
        variable("amount", 1001L);

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(bakeryOrderEndpoint)
            .payload("<order><type>${orderType}</type><id>${orderId}</id><amount>${amount}</amount></order>"));

        echo("Receive report mail for 1000+ order");

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(mailServer)
            .payload(new ClassPathResource("templates/mail.xml"))
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Congratulations!")
            .header(CitrusMailMessageHeaders.MAIL_FROM, "cookie-report@example.com")
            .header(CitrusMailMessageHeaders.MAIL_TO, "stakeholders@example.com"));

        send(sendMesageBuilder -> sendMesageBuilder
            .endpoint(mailServer)
            .payload(new ClassPathResource("templates/mail_response.xml")));

        echo("Receive report with 1000+ order");

        http(httpActionBuilder -> httpActionBuilder
            .client(reportingClient)
            .send()
            .get("/reporting/json"));

        http(httpActionBuilder -> httpActionBuilder
            .client(reportingClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{\"caramel\": \"@ignore@\",\"blueberry\": \"@ignore@\",\"chocolate\": \"@greaterThan(1000)@\"}"));
    }
}
