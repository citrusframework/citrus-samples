package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-plain-form-data.citrus.it.xml" })
    public void testPlainFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-form-data.citrus.it.xml" })
    public void testFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-form-data-xml.citrus.it.xml" })
    public void testFormDataXml() {
    }
}
