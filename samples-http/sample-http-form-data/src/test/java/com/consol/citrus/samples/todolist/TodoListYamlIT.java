package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-plain-form-data.citrus.it.yaml" })
    public void testPlainFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-form-data.citrus.it.yaml" })
    public void testFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-form-data-xml.citrus.it.yaml" })
    public void testFormDataXml() {
    }
}
