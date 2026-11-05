package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;

public class TodoListXmlIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-get.citrus.it.xml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-post.citrus.it.xml" })
    public void testPost() {
    }
}
