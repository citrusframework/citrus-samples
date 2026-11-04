package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-behavior-payload.citrus.it.yaml" })
    public void testJsonPayloadValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-behavior-file-resource.citrus.it.yaml" })
    public void testJsonValidationWithFileResource() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-behavior-json-path.citrus.it.yaml" })
    public void testJsonPathValidation() {
    }
}
