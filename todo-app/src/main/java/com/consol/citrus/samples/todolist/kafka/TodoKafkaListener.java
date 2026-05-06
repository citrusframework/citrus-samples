package com.consol.citrus.samples.todolist.kafka;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
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
public class TodoKafkaListener {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(TodoKafkaListener.class);

    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    @Autowired
    private TodoListService todoListService;

    @KafkaListener(topics = "todo.inbound", groupId = "todo-app-group")
    public void receiveTodo(ConsumerRecord<Integer, String> todoRecord) {
        try {
            TodoEntry entry = mapper.readValue(todoRecord.value(), TodoEntry.class);
            todoListService.addEntry(entry);
        } catch (JacksonException e) {
            log.error("Failed to create todo entry from Kafka record", e);
        }
    }
}
