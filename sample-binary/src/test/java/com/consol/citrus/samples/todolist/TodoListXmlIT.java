package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-add-entry-binary-base64.citrus.it.xml" })
    public void testAddTodoEntryBinaryBase64() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-add-entry-binary.citrus.it.xml" })
    public void testAddTodoEntryBinary() {
    }
}
