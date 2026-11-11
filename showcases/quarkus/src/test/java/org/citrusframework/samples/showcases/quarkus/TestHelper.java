package org.citrusframework.samples.showcases.quarkus;

import com.github.javafaker.Faker;
import org.citrusframework.samples.showcases.quarkus.model.ShippingAddress;

public final class TestHelper {

    private static final Faker faker = new Faker();

    private TestHelper() {
        // prevent instantiation of utility class.
    }

    public static ShippingAddress createShippingAddress() {
        return new ShippingAddress(faker.name().fullName(), faker.address().streetAddress());
    }
}
