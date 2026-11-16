package org.citrusframework.samples.runtimes.junitjupiter;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class TodoListYamlIT {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/junitjupiter/todo-get.citrus.it.yaml" })
    void testGet() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/samples/runtimes/junitjupiter/todo-post.citrus.it.yaml" })
    void testPost() {
    }
}
