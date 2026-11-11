package com.consol.citrus.samples.flightbooking;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class FlightBookingYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/flightbooking/FlightBookingIT.citrus.it.yaml")
    public void testFlightBooking() {
    }
}
