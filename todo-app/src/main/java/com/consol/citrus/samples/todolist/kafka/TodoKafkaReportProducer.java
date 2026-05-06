package com.consol.citrus.samples.todolist.kafka;

import java.util.List;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Christoph Deppisch
 */
@Component
@ConditionalOnProperty(prefix = "todo.kafka", value = "enabled")
public class TodoKafkaReportProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    /**
     * Send entries to Kafka topic.
     */
    public void sendReport(List<TodoEntry> entries) {
        try {
            String jsonPayload = mapper.writeValueAsString(entries);
            kafkaTemplate.send("todo.report", "todo.entries.done", jsonPayload);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to send Kafka todo report", e);
        }
    }
}
