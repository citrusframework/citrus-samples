package org.citrusframework.samples.runtimes.junit4;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;

public class TodoListYamlIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/junit4/todo-get.citrus.it.yaml" })
    public void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/junit4/todo-post.citrus.it.yaml" })
    public void testPost() {
    }
}
