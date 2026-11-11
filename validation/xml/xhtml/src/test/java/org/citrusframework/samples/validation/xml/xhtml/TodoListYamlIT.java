package org.citrusframework.samples.validation.xml.xhtml;

import org.citrusframework.api.common.TestLoader;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/xml/xhtml/todo-index-page.citrus.it.yaml" })
    public void testIndexPage() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/validation/xml/xhtml/todo-add-entry.citrus.it.yaml" })
    public void testAddTodoEntry() {
    }
}
