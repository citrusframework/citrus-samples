package org.apache.camel.demo;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.ShippingAddress;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Singleton
public class ShippingService {

    @Inject
    @RestClient
    ShippingDetailsClient shippingDetailsClient;

    public ShippingAddress getAddressInformation(Booking booking) {
        return shippingDetailsClient.getAddressInformation(booking.getClient());
    }
}
