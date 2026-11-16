package org.citrusframework.samples.endpoints.kafka;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:org/citrusframework/samples/endpoints/kafka/todo-add-entry.citrus.it.yaml")
    public void testAddTodoEntry() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:org/citrusframework/samples/endpoints/kafka/todo-report-done.citrus.it.yaml")
    public void testReportTodoEntryDone() {
    }
}
