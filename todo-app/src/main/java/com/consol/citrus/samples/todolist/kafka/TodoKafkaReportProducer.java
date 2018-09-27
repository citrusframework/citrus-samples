package com.consol.citrus.samples.todolist.kafka;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Christoph Deppisch
 */
@Component
@ConditionalOnProperty(prefix = "todo.kafka", value = "enabled")
public class TodoKafkaReportProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send entries to Kafka topic.
     */
    public void sendReport(List<TodoEntry> entries) {
        try {
            String jsonPayload = new ObjectMapper().writeValueAsString(entries);
            kafkaTemplate.send("todo.report", "todo.entries.done", jsonPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to send Kafka todo report", e);
        }
    }
}
