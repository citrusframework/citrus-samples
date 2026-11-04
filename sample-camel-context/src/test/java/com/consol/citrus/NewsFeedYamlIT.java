package com.consol.citrus;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class NewsFeedYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/news-feed-ok-1.citrus.it.yaml" })
    public void testNewsFeedOk1() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/news-feed-ok-2.citrus.it.yaml" })
    public void testNewsFeedOk2() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/news-feed-data-provider-ok.citrus.it.yaml" })
    public void testNewsFeedDataProviderOk() {
    }
}
