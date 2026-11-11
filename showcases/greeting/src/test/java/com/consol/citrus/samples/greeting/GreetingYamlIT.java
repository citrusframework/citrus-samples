package com.consol.citrus.samples.greeting;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class GreetingYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/greeting/GreetingJms_IT.citrus.it.yaml")
    public void testGreetingJms() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/greeting/GreetingChannel_IT.citrus.it.yaml")
    public void testGreetingChannel() {
    }
}
