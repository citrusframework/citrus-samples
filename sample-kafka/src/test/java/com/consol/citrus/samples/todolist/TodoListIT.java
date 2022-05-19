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

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.kafka.endpoint.KafkaEndpoint;
import com.consol.citrus.kafka.message.KafkaMessage;
import com.consol.citrus.kafka.message.KafkaMessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static com.consol.citrus.dsl.XpathSupport.xpath;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    @Qualifier("todoKafkaEndpoint")
    private KafkaEndpoint todoKafkaEndpoint;

    @Autowired
    @Qualifier("todoReportEndpoint")
    private KafkaEndpoint todoReportEndpoint;

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(send()
            .endpoint(todoKafkaEndpoint)
            .message()
            .header(KafkaMessageHeaders.MESSAGE_KEY, "${todoName}")
            .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }"));

        $(repeatOnError().until((i, context) -> i > 5)
                .actions(
                    getTodoEntries(),
                    verifyTodoEntry()
                ));
    }

    private TestActionBuilder<?> getTodoEntries() {
        return http()
                .client(todoClient)
                .send()
                .get("/todolist")
                .message()
                .accept(MediaType.TEXT_HTML_VALUE);
    }

    private TestActionBuilder<?> verifyTodoEntry() {
        return http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .type(MessageType.XHTML)
                .validate(xpath()
                        .expression("(//xh:li[@class='list-group-item']/xh:span)[last()]", "${todoName}"));
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
            .message(new KafkaMessage("[{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"attachment\":null, \"done\":true}]")
                    .messageKey("todo.entries.done"))
            .type(MessageType.JSON));
    }
}
