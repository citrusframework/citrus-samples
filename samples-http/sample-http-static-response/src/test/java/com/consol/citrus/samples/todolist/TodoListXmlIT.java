package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-static-todo.citrus.it.xml" })
    public void testTodo() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-static-todolist.citrus.it.xml" })
    public void testTodoList() {
    }
}
