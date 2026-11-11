package org.citrusframework.samples.endpoints.http.loadtest;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListLoadTestYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/loadtest/todo-load-add.citrus.it.yaml" })
    public void testAddTodo() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/loadtest/todo-load-list.citrus.it.yaml" })
    public void testListTodos() {
    }
}
