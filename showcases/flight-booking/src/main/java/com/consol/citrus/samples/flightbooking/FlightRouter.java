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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.samples.flightbooking.model.FlightBookingRequestMessage;
import org.springframework.integration.annotation.Router;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/**
 * @author Christoph Deppisch
 */
public class FlightRouter {

    private Map<String, MessageChannel> airlineMappings = new HashMap<String, MessageChannel>();

    @Router
    public Collection<MessageChannel> determineTargetChannels(Message<?> message) {

        FlightBookingRequestMessage request = (FlightBookingRequestMessage)message.getPayload();

        return Collections.singletonList(airlineMappings.get(request.getFlight().getAirline()));
    }

    /**
     * @param airlineMappings the airlineMappings to set
     */
    public void setAirlineMappings(Map<String, MessageChannel> airlineMappings) {
        this.airlineMappings = airlineMappings;
    }
}
