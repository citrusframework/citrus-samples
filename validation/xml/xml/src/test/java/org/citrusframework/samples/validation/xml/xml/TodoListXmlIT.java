package org.citrusframework.samples.validation.xml.xml;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/xml/xml/todo-xml-dom-tree-validation.citrus.it.xml" })
    public void testXmlDomTreeValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/xml/xml/todo-xml-file-resource.citrus.it.xml" })
    public void testXmlValidationWithFileResource() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/validation/xml/xml/todo-xpath-validation.citrus.it.xml" })
    public void testXpathValidation() {
    }
}
