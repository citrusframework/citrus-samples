package org.citrusframework.samples.core.annotationconfig;

import org.citrusframework.samples.apps.todoapp.TodoAppAutoConfiguration;
import java.util.Collections;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { TodoAppAutoConfiguration.class, TodoListYamlIT.ClientConfig.class })
public class TodoListYamlIT extends TestNGCitrusSpringSupport {

    @Configuration
    public static class ClientConfig {

        @Bean
        public HttpClient todoClient() {
            return CitrusEndpoints.http()
                    .client()
                    .requestUrl("http://localhost:8080")
                    .build();
        }

        @Bean
        public NamespaceContextBuilder namespaceContextBuilder() {
            NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
            namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("xh", "http://www.w3.org/1999/xhtml"));
            return namespaceContextBuilder;
        }
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/core/annotationconfig/todo-get.citrus.it.yaml" })
    public void testGet() {
    }
}
