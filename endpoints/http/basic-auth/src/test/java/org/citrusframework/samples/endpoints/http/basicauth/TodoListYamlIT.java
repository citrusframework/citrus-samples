package org.citrusframework.samples.endpoints.http.basicauth;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth.citrus.it.yaml" })
    public void testBasicAuth() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-header.citrus.it.yaml" })
    public void testBasicAuthHeader() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-missing.citrus.it.yaml" })
    public void testBasicAuthMissing() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-failed.citrus.it.yaml" })
    public void testBasicAuthFailed() {
    }
}
