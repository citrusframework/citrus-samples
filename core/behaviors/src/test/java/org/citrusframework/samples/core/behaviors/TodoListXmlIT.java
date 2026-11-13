package org.citrusframework.samples.core.behaviors;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/behaviors/todo-behavior-payload.citrus.it.xml" })
    public void testJsonPayloadValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/behaviors/todo-behavior-file-resource.citrus.it.xml" })
    public void testJsonValidationWithFileResource() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/behaviors/todo-behavior-json-path.citrus.it.xml" })
    public void testJsonPathValidation() {
    }
}
