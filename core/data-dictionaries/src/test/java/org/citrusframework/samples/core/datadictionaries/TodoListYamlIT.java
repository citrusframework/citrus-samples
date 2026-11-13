package org.citrusframework.samples.core.datadictionaries;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/datadictionaries/todo-dictionary-payload.citrus.it.yaml" })
    public void testJsonPayloadValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/datadictionaries/todo-dictionary-file-resource.citrus.it.yaml" })
    public void testJsonValidationWithFileResource() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/datadictionaries/todo-dictionary-json-path.citrus.it.yaml" })
    public void testJsonPathValidation() {
    }
}
