package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/todolist/todo-add-entry.citrus.it.yaml")
    public void testAddTodoEntry() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = "classpath:com/consol/citrus/samples/todolist/todo-report-done.citrus.it.yaml")
    public void testReportTodoEntryDone() {
    }
}
