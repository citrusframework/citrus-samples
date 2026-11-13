package org.citrusframework.samples.validation.hamcrest;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/hamcrest/todo-hamcrest-validation.citrus.it.xml" })
    public void testHamcrestValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/hamcrest/todo-hamcrest-condition.citrus.it.xml" })
    public void testHamcrestCondition() {
    }
}
