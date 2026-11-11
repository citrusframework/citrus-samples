package org.citrusframework.samples.endpoints.http.formdata;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-plain-form-data.citrus.it.yaml" })
    public void testPlainFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-form-data.citrus.it.yaml" })
    public void testFormData() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/formdata/todo-form-data-xml.citrus.it.yaml" })
    public void testFormDataXml() {
    }
}
