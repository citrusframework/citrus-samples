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

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.reactivestreams.Publisher;

/**
 * @author Christoph Deppisch
 */
@Path("/")
public class FoodMarketResource {

    @Inject
    Template index;

    @Inject
    @Channel("booking-events-stream")
    Publisher<String> bookingEvents;

    @Inject
    @Channel("supply-events-stream")
    Publisher<String> supplyEvents;

    @Inject
    BookingService bookingService;

    @Inject
    SupplyService supplyService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.data("bookings", bookingService.findAll())
                    .data("supplies", supplyService.findAll());
    }

    @GET
    @Path("/bookings")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> bookingProcessor() {
        return bookingEvents;
    }

    @GET
    @Path("/supplies")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> supplyProcessor() {
        return supplyEvents;
    }

}
