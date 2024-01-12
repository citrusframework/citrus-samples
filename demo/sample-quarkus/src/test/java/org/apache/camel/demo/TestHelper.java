package org.apache.camel.demo;

import com.github.javafaker.Faker;
import org.apache.camel.demo.model.ShippingAddress;

public final class TestHelper {

    private static final Faker faker = new Faker();

    private TestHelper() {
        // prevent instantiation of utility class.
    }

    public static ShippingAddress createShippingAddress() {
        return new ShippingAddress(faker.name().fullName(), faker.address().streetAddress());
    }
}
