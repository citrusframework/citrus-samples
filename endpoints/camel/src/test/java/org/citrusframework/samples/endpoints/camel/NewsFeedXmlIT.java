package org.citrusframework.samples.endpoints.camel;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class NewsFeedXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/camel/news-feed-ok-1.citrus.it.xml" })
    public void testNewsFeedOk1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/camel/news-feed-ok-2.citrus.it.xml" })
    public void testNewsFeedOk2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/camel/news-feed-data-provider-ok.citrus.it.xml" })
    public void testNewsFeedDataProviderOk() {
    }
}
