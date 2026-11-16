package org.citrusframework.samples.buildtools.gradle;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { EndpointConfig.class })
public class MessagingXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/buildtools/gradle/messaging.citrus.it.xml" })
    public void testMessaging() {
    }
}
