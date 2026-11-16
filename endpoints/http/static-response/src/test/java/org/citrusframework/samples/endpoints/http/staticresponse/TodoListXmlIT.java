package org.citrusframework.samples.endpoints.http.staticresponse;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/staticresponse/todo-static-todo.citrus.it.xml" })
    public void testTodo() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/staticresponse/todo-static-todolist.citrus.it.xml" })
    public void testTodoList() {
    }
}
