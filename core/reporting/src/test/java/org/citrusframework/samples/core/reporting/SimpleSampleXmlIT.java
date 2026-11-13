package org.citrusframework.samples.core.reporting;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class SimpleSampleXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-first.citrus.it.xml" })
    public void testSuccessFirst() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-second.citrus.it.xml" })
    public void testSuccessSecond() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-third.citrus.it.xml" })
    public void testSuccessThird() {
    }
}
