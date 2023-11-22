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
import io.smallrye.reactive.messaging.annotations.Merge;
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
public class SupplyEvents {

    private static final Logger LOG = Logger.getLogger(SupplyEvents.class);

    @Inject
    SupplyService supplyService;

    @Inject
    BookingService bookingService;

    @Inject
    ProductService productService;

    @Inject
    MailService mailService;

    @Inject
    ObjectMapper mapper;

    @Incoming("supply-events")
    @Outgoing("supply-added")
    @Transactional
    public Supply processEvent(Supply supply) throws JsonProcessingException {
        LOG.info(String.format("Processing supply for product: %s", supply.getProduct().getName()));
        if (supply.getProduct().getId() == null) {
            Optional<Product> existing = productService.findByName(supply.getProduct().getName());
            existing.ifPresentOrElse(supply::setProduct, () -> productService.add(supply.getProduct()));
        }

        supplyService.add(supply);
        LOG.info("New supply:" + mapper.writeValueAsString(supply));
        return supply;
    }

    @Incoming("supply-added")
    @Merge
    @Transactional
    public void onAdded(Supply supply) throws JsonProcessingException {
        Optional<Booking> matchingBooking;
        while ((matchingBooking = bookingService.findMatching(supply)).isPresent()) {
            LOG.info("Found matching booking id=%s for supply: %s".formatted(matchingBooking.get().getId(), mapper.writeValueAsString(supply)));
            Booking booking = matchingBooking.get();
            booking.setPrice(supply.getPrice());
            bookingService.complete(booking);

            supplyService.adjust(supply, booking);

            mailService.send(booking);
        }
    }

}
