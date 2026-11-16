package org.citrusframework.samples.connectors.selenium;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoPagesYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/connectors/selenium/todo-pages-index-page.citrus.it.yaml" })
    public void testIndexPage() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/connectors/selenium/todo-pages-add-entry.citrus.it.yaml" })
    public void testAddEntry() {
    }
}
