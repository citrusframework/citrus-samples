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
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.apache.camel.demo.model.Supply;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

/**
 * @author Christoph Deppisch
 */
@Path("/")
public class FoodMarketResource {

    private static final Logger LOG = Logger.getLogger(FoodMarketResource.class);

    @Inject
    Template index;

    @Inject
    @Channel("booking-added-events-stream")
    Publisher<String> bookingEvents;

    @Inject
    @Channel("supply-added-events-stream")
    Publisher<String> supplyEvents;

    @Inject
    BookingService bookingService;

    @Inject
    SupplyService supplyService;

    @Inject
    ProductService productService;

    @Inject
    ObjectMapper mapper;

    @Inject
    @Channel("booking-added")
    MutinyEmitter<Booking> bookingAddedEmitter;

    @Inject
    @Channel("supply-added")
    MutinyEmitter<Supply> supplyAddedEmitter;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.data("bookings", bookingService.findAll())
                    .data("supplies", supplyService.findAll())
                    .data("products", productService.findAll());
    }

    @GET
    @Path("bookings")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> bookingProcessor() {
        return bookingEvents;
    }

    @GET
    @Path("supplies")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> supplyProcessor() {
        return supplyEvents;
    }

    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public TemplateInstance formSubmit(MultivaluedMap<String, String> form) throws JsonProcessingException {
        String type = form.getFirst("type");
        String productName = form.getFirst("product");
        LOG.info(String.format("Processing booking for product: %s", productName));
        Optional<Product> product = productService.findByName(productName);
        if (product.isEmpty()) {
            LOG.info(String.format("Failed to find product: %s", productName));
            return index();
        }

        if ("booking".equals(type)) {
            Booking booking = new Booking(form.getFirst("name"), product.get(),
                    Integer.parseInt(form.getFirst("amount")), Double.parseDouble(form.getFirst("price")));

            bookingService.add(booking);
            LOG.info("New booking: " + mapper.writeValueAsString(booking));

            bookingAddedEmitter.send(booking).subscribe()
                    .with(
                            success -> LOG.info("Booking added event successfully sent"),
                            failure -> LOG.info("Booking added event failed: " + failure.getMessage())
                    );
        } else if ("supply".equals(type)) {
            Supply supply = new Supply(form.getFirst("name"), product.get(),
                    Integer.parseInt(form.getFirst("amount")), Double.parseDouble(form.getFirst("price")));

            supplyService.add(supply);
            LOG.info("New supply: " + mapper.writeValueAsString(supply));

            supplyAddedEmitter.send(supply).subscribe()
                    .with(
                            success -> LOG.info("Supply added event successfully sent"),
                            failure -> LOG.info("Supply added event failed: " + failure.getMessage())
                    );
        }
        return index();
    }
}
