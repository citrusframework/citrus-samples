package org.citrusframework.samples.endpoints.http.basic;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-get.citrus.it.xml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-post.citrus.it.xml" })
    public void testPost() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-lifecycle.citrus.it.xml" })
    public void testTodoLifecycle() {
    }
}
