package org.citrusframework.samples.core.dynamicendpoints;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/dynamicendpoints/todo-http-add.citrus.it.yaml" })
    public void testHttpAddTodoEntry() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/dynamicendpoints/todo-jms-add.citrus.it.yaml" })
    public void testAddTodoEntry() {
    }
}
