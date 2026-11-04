package com.consol.citrus.samples.todolist;

import java.util.Collections;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { EndpointConfig.class, TodoListYamlIT.NsConfig.class })
public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Configuration
    public static class NsConfig {

        @Bean
        public NamespaceContextBuilder namespaceContextBuilder() {
            NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
            namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("xh", "http://www.w3.org/1999/xhtml"));
            return namespaceContextBuilder;
        }
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/samples/todolist/todo-get.citrus.it.yaml" })
    public void testGet() {
    }
}
