package com.consol.citrus.samples.todolist.jms;

import java.util.List;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
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
@ConditionalOnProperty(prefix = "todo.jms", value = "enabled")
public class TodoJmsReportProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    /**
     * Send entries to Jms queue.
     */
    public void sendReport(List<TodoEntry> entries) {
        try {
            String jsonPayload = mapper.writeValueAsString(entries);
            jmsTemplate.send("jms.todo.report", session -> {
                TextMessage message = session.createTextMessage(jsonPayload);
                message.setStringProperty("_type", TodoEntry.class.getName());
                return message;
            });
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to send Jms todo report", e);
        }
    }
}
