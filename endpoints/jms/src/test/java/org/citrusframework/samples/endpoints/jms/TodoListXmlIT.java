package org.citrusframework.samples.endpoints.jms;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/jms/todo-add-entry.citrus.it.xml" })
    public void testAddTodoEntry() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/jms/todo-report-done.citrus.it.xml" })
    public void testReportTodoEntryDone() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/jms/todo-sync-add-entry.citrus.it.xml" })
    public void testSyncAddTodoEntry() {
    }
}
