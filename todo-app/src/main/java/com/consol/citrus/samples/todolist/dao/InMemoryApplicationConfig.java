package com.consol.citrus.samples.todolist.dao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
@ConditionalOnProperty(prefix = "todo.persistence", value = "type", havingValue = "in_memory", matchIfMissing = true)
public class InMemoryApplicationConfig {

    @Bean
    public TodoListDao todoListInMemoryDao() {
        return new InMemoryTodoListDao();
    }
}
