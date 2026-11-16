package org.citrusframework.samples.showcases.greeting;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class GreetingXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/greeting/GreetingJms_IT.citrus.it.xml")
    public void testGreetingJms() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/samples/showcases/greeting/GreetingChannel_IT.citrus.it.xml")
    public void testGreetingChannel() {
    }
}
