package org.citrusframework.samples.showcases.flightbooking;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class FlightBookingYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:org/citrusframework/samples/showcases/flightbooking/FlightBookingIT.citrus.it.yaml")
    public void testFlightBooking() {
    }
}
