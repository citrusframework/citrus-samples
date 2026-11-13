package org.citrusframework.samples.connectors.selenium;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoPagesXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/connectors/selenium/todo-pages-index-page.citrus.it.xml" })
    public void testIndexPage() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/connectors/selenium/todo-pages-add-entry.citrus.it.xml" })
    public void testAddEntry() {
    }
}
