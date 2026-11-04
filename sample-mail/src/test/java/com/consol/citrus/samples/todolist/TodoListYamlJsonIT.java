package com.consol.citrus.samples.todolist;



import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlJsonIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:com/consol/citrus/samples/todolist/todo-mail-report-json.citrus.it.yaml"})
    public void testMailReportJson() {
    }
}
