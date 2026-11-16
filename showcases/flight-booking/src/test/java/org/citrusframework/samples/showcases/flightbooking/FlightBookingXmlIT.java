package org.citrusframework.samples.showcases.flightbooking;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class FlightBookingXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/flightbooking/FlightBookingIT.citrus.it.xml")
    public void testFlightBooking() {
    }
}
