/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.samples.flightbooking;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static com.consol.citrus.dsl.XmlSupport.xml;
import static com.consol.citrus.dsl.XpathSupport.xpath;

/**
 * @author Christoph Deppisch
 */
@Test
public class FlightBookingIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("travelAgencyBookingRequestEndpoint")
    private JmsEndpoint travelAgencyBookingRequestEndpoint;

    @Autowired
    @Qualifier("travelAgencyBookingResponseEndpoint")
    private JmsEndpoint travelAgencyBookingResponseEndpoint;

    @Autowired
    @Qualifier("smartAirlineBookingRequestEndpoint")
    private JmsEndpoint smartAirlineBookingRequestEndpoint;

    @Autowired
    @Qualifier("smartAirlineBookingResponseEndpoint")
    private JmsEndpoint smartAirlineBookingResponseEndpoint;

    @Autowired
    private HttpServer royalAirlineServer;

    @CitrusTest(name = "FlightBookingIT")
    public void flightBookingIT() {
        variable("correlationId", "citrus:concat('Lx1x', 'citrus:randomNumber(10)')");
        variable("customerId", "citrus:concat('Mx1x', citrus:randomNumber(10))");

        $(send()
            .endpoint(travelAgencyBookingRequestEndpoint)
            .message()
            .body(new ClassPathResource("templates/TravelBookingRequest.xml"))
                .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(royalAirlineServer)
            .message()
            .body(new ClassPathResource("templates/RoyalAirlineBookingRequest.xml"))
            .header("bookingCorrelationId", "${correlationId}")
            .validate(xml()
                        .xpath()
                        .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId"))
            .extract(fromHeaders()
                        .expression("X-sequenceNumber", "${sequenceNumber}")
                        .expression("X-sequenceSize", "${sequenceSize}"))
            .extract(xpath()
                    .expression("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${royalAirlineBookingId}")));

        $(send()
            .endpoint(royalAirlineServer)
            .message()
            .body(new ClassPathResource("templates/RoyalAirlineBookingResponse.xml"))
            .header("X-sequenceNumber", "${sequenceNumber}")
            .header("X-sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(smartAirlineBookingRequestEndpoint)
            .message()
            .body(new ClassPathResource("templates/SmartAirlineBookingRequest.xml"))
            .validate(xml()
                        .xpath()
                        .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId"))
            .header("bookingCorrelationId", "${correlationId}")
            .extract(fromHeaders()
                        .expression("sequenceNumber", "${sequenceNumber}")
                        .expression("sequenceSize", "${sequenceSize}"))
            .extract(xpath()
                        .expression("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${smartAirlineBookingId}")));

        $(send()
            .endpoint(smartAirlineBookingResponseEndpoint)
            .message()
            .body(new ClassPathResource("templates/SmartAirlineBookingResponse.xml"))
            .header("sequenceNumber", "${sequenceNumber}")
            .header("sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(travelAgencyBookingResponseEndpoint)
            .message()
            .body(new ClassPathResource("templates/TravelBookingResponse.xml"))
            .header("bookingCorrelationId", "${correlationId}"));
    }

}
