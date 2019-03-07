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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class FlightBookingIT extends TestNGCitrusTestRunner {

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

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(travelAgencyBookingRequestEndpoint)
            .payload(new ClassPathResource("templates/TravelBookingRequest.xml"))
                .header("bookingCorrelationId", "${correlationId}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(royalAirlineServer)
            .payload(new ClassPathResource("templates/RoyalAirlineBookingRequest.xml"))
            .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId")
            .header("bookingCorrelationId", "${correlationId}")
            .extractFromHeader("X-sequenceNumber", "${sequenceNumber}")
            .extractFromHeader("X-sequenceSize", "${sequenceSize}")
            .extractFromPayload("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${royalAirlineBookingId}"));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(royalAirlineServer)
            .payload(new ClassPathResource("templates/RoyalAirlineBookingResponse.xml"))
            .header("X-sequenceNumber", "${sequenceNumber}")
            .header("X-sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(smartAirlineBookingRequestEndpoint)
            .payload(new ClassPathResource("templates/SmartAirlineBookingRequest.xml"))
            .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId")
            .header("bookingCorrelationId", "${correlationId}")
            .extractFromHeader("sequenceNumber", "${sequenceNumber}")
            .extractFromHeader("sequenceSize", "${sequenceSize}")
            .extractFromPayload("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${smartAirlineBookingId}"));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(smartAirlineBookingResponseEndpoint)
            .payload(new ClassPathResource("templates/SmartAirlineBookingResponse.xml"))
            .header("sequenceNumber", "${sequenceNumber}")
            .header("sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(travelAgencyBookingResponseEndpoint)
            .payload(new ClassPathResource("templates/TravelBookingResponse.xml"))
            .header("bookingCorrelationId", "${correlationId}"));
    }

}
