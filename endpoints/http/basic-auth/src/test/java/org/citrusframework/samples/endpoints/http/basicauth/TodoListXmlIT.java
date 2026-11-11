package org.citrusframework.samples.endpoints.http.basicauth;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth.citrus.it.xml" })
    public void testBasicAuth() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-header.citrus.it.xml" })
    public void testBasicAuthHeader() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-missing.citrus.it.xml" })
    public void testBasicAuthMissing() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/endpoints/http/basicauth/todo-basic-auth-failed.citrus.it.xml" })
    public void testBasicAuthFailed() {
    }
}
