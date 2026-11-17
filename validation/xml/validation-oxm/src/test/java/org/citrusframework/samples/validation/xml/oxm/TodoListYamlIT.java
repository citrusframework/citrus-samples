package org.citrusframework.samples.validation.xml.oxm;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/xml/oxm/todo-object-marshalling.citrus.it.yaml" })
    public void testObjectMarshalling() {
    }
}
