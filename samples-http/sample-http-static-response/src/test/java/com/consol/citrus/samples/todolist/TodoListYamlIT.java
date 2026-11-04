package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-static-todo.citrus.it.yaml" })
    public void testTodo() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-static-todolist.citrus.it.yaml" })
    public void testTodoList() {
    }
}
