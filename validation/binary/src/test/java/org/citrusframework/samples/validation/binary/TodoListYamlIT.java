package org.citrusframework.samples.validation.binary;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/binary/todo-add-entry-binary-base64.citrus.it.yaml" })
    public void testAddTodoEntryBinaryBase64() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/binary/todo-add-entry-binary.citrus.it.yaml" })
    public void testAddTodoEntryBinary() {
    }
}
