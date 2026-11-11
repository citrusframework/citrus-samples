package org.citrusframework.samples.showcases.quarkus.behavior;

import org.citrusframework.samples.showcases.quarkus.TestHelper;
import org.citrusframework.samples.showcases.quarkus.model.ShippingAddress;
import org.citrusframework.TestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestBehavior;
import org.citrusframework.http.server.HttpServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.citrusframework.json.dsl.JsonSupport.marshal;

public class GetShippingAddress implements TestBehavior, TestActionSupport {

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
