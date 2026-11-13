package org.citrusframework.samples.core.reporting;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class SimpleSampleYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-first.citrus.it.yaml" })
    public void testSuccessFirst() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-second.citrus.it.yaml" })
    public void testSuccessSecond() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/reporting/simple-success-third.citrus.it.yaml" })
    public void testSuccessThird() {
    }
}
