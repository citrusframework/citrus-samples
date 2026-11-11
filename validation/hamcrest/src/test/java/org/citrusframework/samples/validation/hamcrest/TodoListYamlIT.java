package org.citrusframework.samples.validation.hamcrest;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/hamcrest/todo-hamcrest-validation.citrus.it.yaml" })
    public void testHamcrestValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/hamcrest/todo-hamcrest-condition.citrus.it.yaml" })
    public void testHamcrestCondition() {
    }
}
