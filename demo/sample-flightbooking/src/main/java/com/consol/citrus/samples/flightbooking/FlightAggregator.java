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

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.samples.flightbooking.model.Flight;
import com.consol.citrus.samples.flightbooking.model.FlightBookingConfirmationMessage;
import com.consol.citrus.samples.flightbooking.model.TravelBookingResponseMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * @author Christoph Deppisch
 */
public class FlightAggregator {

    public Message<TravelBookingResponseMessage> processFlights(List<FlightBookingConfirmationMessage> messages) {
        TravelBookingResponseMessage responseMessage = new TravelBookingResponseMessage();

        List<Flight> flights = new ArrayList<Flight>();
        for (FlightBookingConfirmationMessage confirmationMessage : messages) {
            flights.add(confirmationMessage.getFlight());
        }

        TravelBookingResponseMessage.Flights flightContainer = new TravelBookingResponseMessage.Flights();
        flightContainer.getFlights().addAll(flights);
        responseMessage.setFlights(flightContainer);
        responseMessage.setCorrelationId(messages.get(0).getCorrelationId());
        responseMessage.setSuccess(true);

        MessageBuilder<TravelBookingResponseMessage> messageBuilder = MessageBuilder.withPayload(responseMessage);
        messageBuilder.setHeader("correlationId", responseMessage.getCorrelationId());

        return messageBuilder.build();
    }
}
