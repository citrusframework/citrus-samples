package com.consol.citrus.samples.todolist.kafka;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Component
@ConditionalOnProperty(prefix = "todo.kafka", value = "enabled")
public class TodoKafkaListener {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TodoKafkaListener.class);

    @Autowired
    private TodoListService todoListService;

    @KafkaListener(topics = "todo.inbound")
    public void receiveTodo(ConsumerRecord<Integer, String> todoRecord) {
        try {
            TodoEntry entry = new ObjectMapper().readValue(todoRecord.value(), TodoEntry.class);
            todoListService.addEntry(entry);
        } catch (IOException e) {
            log.error("Failed to create todo entry from Kafka record", e);
        }
    }
}
