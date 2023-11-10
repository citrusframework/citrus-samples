/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.demo;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.apache.camel.demo.model.Supply;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BookingEvents {

    private static final Logger LOG = Logger.getLogger(BookingEvents.class);

    @Inject
    BookingService bookingService;

    @Inject
    SupplyService supplyService;

    @Inject
    ProductService productService;

    @Inject
    MailService mailService;

    @Inject
    ObjectMapper mapper;

    @Incoming("booking-events")
    @Outgoing("booking-added")
    @Transactional
    public Booking processEvent(Booking booking) throws JsonProcessingException {
        LOG.info(String.format("Processing booking for product: %s", booking.getProduct().getName()));
        if (booking.getProduct().getId() == null) {
            Optional<Product> existing = productService.findByName(booking.getProduct().getName());
            existing.ifPresentOrElse(booking::setProduct, () -> productService.add(booking.getProduct()));
        }

        bookingService.add(booking);
        LOG.info("New booking: " + mapper.writeValueAsString(booking));
        return booking;
    }

    @Incoming("booking-added")
    @Transactional
    public void onAdded(Booking booking) throws JsonProcessingException {
        Optional<Supply> matchingSupply = supplyService.findAvailable(booking);
        if (matchingSupply.isPresent()) {
            LOG.info("Found matching supply id=%s for booking: %s".formatted(matchingSupply.get().getId(), mapper.writeValueAsString(booking)));

            Supply supply = matchingSupply.get();
            booking.setPrice(supply.getPrice());
            bookingService.complete(booking);

            supplyService.adjust(supply, booking);

            mailService.send(booking);
        }
    }
}
