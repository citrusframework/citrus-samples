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
import com.consol.citrus.dsl.functions.Functions;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
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
public class PlaceOrderAndReceiveMailIT extends TestNGCitrusTestDesigner {

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
    public void shouldSendMail() {
        echo("Add 1000+ order and receive mail");

        variable("orderType", "chocolate");
        variable("orderId", Functions.randomNumber(10L));

        send(bakeryOrderEndpoint)
                .payload("<order type=\"${orderType}\"><id>${orderId}</id><amount>1001</amount></order>");

        echo("Receive report mail for 1000+ order");

        receive(mailServer)
                .payload(new ClassPathResource("templates/mail.xml"))
                .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Congratulations!")
                .header(CitrusMailMessageHeaders.MAIL_FROM, "cookie-report@example.com")
                .header(CitrusMailMessageHeaders.MAIL_TO, "stakeholders@example.com");

        echo("Receive report with 1000+ order");

        http().client(reportingClient)
                .get("/reporting/json");

        http().client(reportingClient)
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .payload("{\"caramel\": \"@ignore@\",\"blueberry\": \"@ignore@\",\"chocolate\": \"@greaterThan(1000)@\"}");
    }
}
