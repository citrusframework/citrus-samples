package org.citrusframework.samples.runtimes.testng;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/testng/todo-get.citrus.it.yaml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/testng/todo-post.citrus.it.yaml" })
    public void testPost() {
    }
}
