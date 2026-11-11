package org.citrusframework.samples.core.messagestore;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class TodoListXmlIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/messagestore/todo-message-store-validation.citrus.it.xml" })
    public void testMessageStoreValidation() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/samples/core/messagestore/todo-message-store-callback.citrus.it.xml" })
    public void testMessageStoreValidationCallback() {
    }
}
