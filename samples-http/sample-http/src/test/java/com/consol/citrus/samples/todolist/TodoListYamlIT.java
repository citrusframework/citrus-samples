package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-get.citrus.it.yaml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-post.citrus.it.yaml" })
    public void testPost() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-lifecycle.citrus.it.yaml" })
    public void testTodoLifecycle() {
    }
}
