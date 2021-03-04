package com.consol.citrus.samples.todolist;

import com.consol.citrus.container.BeforeSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
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
    public BeforeSuite embeddedTodoApp() {
        return new SequenceBeforeSuite.Builder()
                .actions(context -> SpringApplication.run(TodoApplication.class))
                .build();
    }
}
