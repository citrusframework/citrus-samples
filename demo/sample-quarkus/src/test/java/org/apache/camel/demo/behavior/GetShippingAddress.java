package org.apache.camel.demo.behavior;

import org.apache.camel.demo.TestHelper;
import org.apache.camel.demo.model.ShippingAddress;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.http.server.HttpServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.citrusframework.dsl.JsonSupport.marshal;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

public class GetShippingAddress implements TestBehavior {

    private final HttpServer shippingService;

    private final ShippingAddress address;

    public GetShippingAddress(HttpServer shippingService) {
        this(shippingService, TestHelper.createShippingAddress());
    }

    public GetShippingAddress(HttpServer shippingService, ShippingAddress address) {
        this.shippingService = shippingService;
        this.address = address;
    }

    @Override
    public void apply(TestActionRunner t) {
        t.run(http().server(shippingService)
                .receive()
                .get("/shipping/address/${booking.client}"));

        t.run(http().server(shippingService)
                .respond(HttpStatus.OK)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(marshal(address)));
    }
}
