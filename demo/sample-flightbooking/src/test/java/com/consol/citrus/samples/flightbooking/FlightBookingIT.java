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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class FlightBookingIT extends TestNGCitrusSpringSupport implements TestActionSupport {

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
            .body(Resources.fromClasspath("templates/TravelBookingRequest.xml"))
                .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(royalAirlineServer)
            .message()
            .body(Resources.fromClasspath("templates/RoyalAirlineBookingRequest.xml"))
            .header("bookingCorrelationId", "${correlationId}")
            .validate(validation()
                        .xpath()
                        .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId"))
            .extract(extractor()
                        .fromHeaders()
                        .expression("X-sequenceNumber", "${sequenceNumber}")
                        .expression("X-sequenceSize", "${sequenceSize}"))
            .extract(extractor()
                    .xpath()
                    .expression("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${royalAirlineBookingId}")));

        $(send()
            .endpoint(royalAirlineServer)
            .message()
            .body(Resources.fromClasspath("templates/RoyalAirlineBookingResponse.xml"))
            .header("X-sequenceNumber", "${sequenceNumber}")
            .header("X-sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(smartAirlineBookingRequestEndpoint)
            .message()
            .body(Resources.fromClasspath("templates/SmartAirlineBookingRequest.xml"))
            .validate(validation()
                        .xpath()
                        .ignore("//fbs:FlightBookingRequestMessage/fbs:bookingId"))
            .header("bookingCorrelationId", "${correlationId}")
            .extract(extractor()
                        .fromHeaders()
                        .expression("sequenceNumber", "${sequenceNumber}")
                        .expression("sequenceSize", "${sequenceSize}"))
            .extract(extractor()
                        .xpath()
                        .expression("//fbs:FlightBookingRequestMessage/fbs:bookingId", "${smartAirlineBookingId}")));

        $(send()
            .endpoint(smartAirlineBookingResponseEndpoint)
            .message()
            .body(Resources.fromClasspath("templates/SmartAirlineBookingResponse.xml"))
            .header("sequenceNumber", "${sequenceNumber}")
            .header("sequenceSize", "${sequenceSize}")
            .header("bookingCorrelationId", "${correlationId}"));

        $(receive()
            .endpoint(travelAgencyBookingResponseEndpoint)
            .message()
            .body(Resources.fromClasspath("templates/TravelBookingResponse.xml"))
            .header("bookingCorrelationId", "${correlationId}"));
    }

}
