package org.citrusframework.samples.endpoints.mail;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlJsonIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:org/citrusframework/samples/endpoints/mail/todo-mail-report-json.citrus.it.yaml"})
    public void testMailReportJson() {
    }
}
