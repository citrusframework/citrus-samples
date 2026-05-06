/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    @Qualifier("todoKafkaEndpoint")
    private KafkaEndpoint todoKafkaEndpoint;

    @Autowired
    @Qualifier("todoReportEndpoint")
    private KafkaEndpoint todoReportEndpoint;

    private final JsonMapper mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(waitFor()
            .http()
            .url(todoClient.getEndpointConfiguration().getRequestUrl() + "/api/todolist"));

        $(send()
            .endpoint(todoKafkaEndpoint)
            .message()
            .header(KafkaMessageHeaders.MESSAGE_KEY, "${todoName}")
            .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }"));

        $(verifyTodoEntry());
    }

    @Test
    @CitrusTest
    public void testReportTodoEntryDone() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(send()
            .endpoint(todoKafkaEndpoint)
            .message()
            .header(KafkaMessageHeaders.MESSAGE_KEY, "${todoName}")
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }"));

        $(verifyTodoEntry());

        $(echo("Set todo entry status to done"));

        $(http()
            .client(todoClient)
            .send()
            .put("/api/todo/${todoId}")
            .queryParam("done", "true")
            .message()
            .accept(MediaType.APPLICATION_JSON_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        $(echo("Trigger Kafka report"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/kafka/report/done")
            .message()
            .accept(MediaType.APPLICATION_JSON_VALUE));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));

        $(receive()
            .endpoint(todoReportEndpoint)
            .message(new KafkaMessage("[{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\":true}]")
                    .messageKey("todo.entries.done"))
            .type(MessageType.JSON));
    }

    private TestActionBuilder<?> verifyTodoEntry() {
        return repeatOnError().until((i, context) -> i > 10)
                .autoSleep(Duration.ofMillis(1000))
                .actions(
                    http()
                        .client(todoClient)
                        .send()
                        .get("/api/todolist")
                        .message()
                        .accept(MediaType.APPLICATION_JSON_VALUE),
                    http()
                        .client(todoClient)
                        .receive()
                        .response(HttpStatus.OK)
                        .message()
                        .validate(new JsonMappingValidationProcessor<>(TodoEntry[].class, mapper) {
                            @Override
                            public void validate(TodoEntry[] entries, Map<String, Object> headers, TestContext context) {
                                Arrays.stream(entries)
                                        .peek(todoEntry -> System.out.println("+++++++++++++++++++++++++++++ " + todoEntry.getTitle()))
                                        .filter(entry -> entry.getTitle().equals(context.getVariable("todoName")))
                                        .findFirst()
                                        .orElseThrow(() -> new ValidationException("Missing todo entry: %s".formatted(context.getVariable("todoName"))));
                            }
                        })
                );
    }

}
