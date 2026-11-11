package org.citrusframework.samples.showcases.quarkus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.citrusframework.samples.showcases.quarkus.model.Booking;
import org.citrusframework.samples.showcases.quarkus.model.ShippingAddress;
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
