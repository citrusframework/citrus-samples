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

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Product;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
public class BookingApiResource {

    private static final Logger LOG = Logger.getLogger(BookingApiResource.class);

    @Inject
    BookingService bookingService;

    @Inject
    ProductService productService;

    @Inject
    @Channel("booking-added")
    MutinyEmitter<Booking> bookingAddedEmitter;

    @Inject
    ObjectMapper mapper;

    @GET
    @Operation(operationId = "listBookings")
    public List<Booking> list() {
        return bookingService.findAll();
    }

    @POST
    @Operation(operationId = "addBooking")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(Booking booking) throws JsonProcessingException {
        LOG.info(String.format("Processing booking for product: %s", booking.getProduct().getName()));
        if (booking.getProduct().getId() == null) {
            Optional<Product> existing = productService.findByName(booking.getProduct().getName());
            existing.ifPresentOrElse(booking::setProduct, () -> productService.add(booking.getProduct()));
        }

        bookingService.add(booking);
        LOG.info("New booking: " + mapper.writeValueAsString(booking));

        bookingAddedEmitter.send(booking).subscribe()
                .with(
                    success -> LOG.info("Booking added event successfully sent"),
                    failure -> LOG.info("Booking added event failed: " + failure.getMessage())
                );

        return Response.status(Response.Status.CREATED)
                .entity(booking).build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getBookingById")
    public Response find(@PathParam("id") String id) {
        if (!Pattern.compile("\\d+").matcher(id).matches()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            return Response.ok(bookingService.findById(Long.valueOf(id))).build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteBooking")
    public Response delete(@PathParam("id") String id) {
        if (!Pattern.compile("\\d+").matcher(id).matches()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (bookingService.remove(Long.valueOf(id))) {
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}

