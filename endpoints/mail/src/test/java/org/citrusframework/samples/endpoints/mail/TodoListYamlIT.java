package org.citrusframework.samples.endpoints.mail;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:org/citrusframework/samples/endpoints/mail/todo-mail-report.citrus.it.yaml"})
    public void testMailReport() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:org/citrusframework/samples/endpoints/mail/todo-mail-report-xml.citrus.it.yaml"})
    public void testMailReportXml() {
    }
}
