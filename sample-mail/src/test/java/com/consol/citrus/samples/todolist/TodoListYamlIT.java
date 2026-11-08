package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:com/consol/citrus/samples/todolist/todo-mail-report.citrus.it.yaml"})
    public void testMailReport() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:com/consol/citrus/samples/todolist/todo-mail-report-xml.citrus.it.yaml"})
    public void testMailReportXml() {
    }
}
