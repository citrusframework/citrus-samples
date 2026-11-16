package org.citrusframework.samples.endpoints.http.formdata;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-plain-form-data.citrus.it.xml" })
    public void testPlainFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-form-data.citrus.it.xml" })
    public void testFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-form-data-xml.citrus.it.xml" })
    public void testFormDataXml() {
    }
}
