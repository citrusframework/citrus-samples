package com.consol.citrus.samples.flightbooking;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class FlightBookingXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/flightbooking/FlightBookingIT.citrus.it.xml")
    public void testFlightBooking() {
    }
}
