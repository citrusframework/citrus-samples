package org.citrusframework.samples.connectors.sql;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/connectors/sql/todo-index-page.citrus.it.xml" })
    public void testIndexPage() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/connectors/sql/todo-add-entry.citrus.it.xml" })
    public void testAddTodoEntry() {
    }
}
