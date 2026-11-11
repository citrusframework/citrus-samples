package com.consol.citrus.samples.greeting;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class GreetingXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/greeting/GreetingJms_IT.citrus.it.xml")
    public void testGreetingJms() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:com/consol/citrus/samples/greeting/GreetingChannel_IT.citrus.it.xml")
    public void testGreetingChannel() {
    }
}
