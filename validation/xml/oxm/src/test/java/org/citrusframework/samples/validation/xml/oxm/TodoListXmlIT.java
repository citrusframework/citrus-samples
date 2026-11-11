package org.citrusframework.samples.validation.xml.oxm;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/xml/oxm/todo-object-marshalling.citrus.it.xml" })
    public void testObjectMarshalling() {
    }
}
