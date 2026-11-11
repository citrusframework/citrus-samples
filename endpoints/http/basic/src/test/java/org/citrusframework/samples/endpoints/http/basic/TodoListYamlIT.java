package org.citrusframework.samples.endpoints.http.basic;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-get.citrus.it.yaml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-post.citrus.it.yaml" })
    public void testPost() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basic/todo-lifecycle.citrus.it.yaml" })
    public void testTodoLifecycle() {
    }
}
