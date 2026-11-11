package org.citrusframework.samples.endpoints.http.staticresponse;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/staticresponse/todo-static-todo.citrus.it.yaml" })
    public void testTodo() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/staticresponse/todo-static-todolist.citrus.it.yaml" })
    public void testTodoList() {
    }
}
