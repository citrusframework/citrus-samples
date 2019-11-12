package com.consol.citrus.samples.todolist;

import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeSuiteSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class TodoAppAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "system.under.test.mode", havingValue = "embedded")
    public TestRunnerBeforeSuiteSupport embeddedTodoApp() {
        return new TestRunnerBeforeSuiteSupport() {
            @Override
            public void beforeSuite(TestRunner runner) {
                SpringApplication.run(TodoApplication.class);
            }
        };
    }
}
