/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.samples.kubernetes;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.WatchEventResult;
import org.citrusframework.message.MessageType;
import io.fabric8.kubernetes.client.Watcher;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.actions.StopTimerAction.Builder.stopTimer;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.Timer.Builder.timer;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.kubernetes.actions.KubernetesExecuteAction.Builder.kubernetes;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testDeploymentState() {
        $(kubernetes()
            .client(k8sClient)
            .pods()
            .list()
            .label("app=todo")
            .validate("$..status.phase", "Running")
            .validate((pods, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(pods.getResult().getItems()));
            }));

        $(kubernetes()
            .client(k8sClient)
            .services()
            .get("citrus-sample-todo-service")
            .validate((service, context) -> Assert.assertNotNull(service.getResult())));
    }

    @Test
    @CitrusTest
    public void testTodoService() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @Test
    @CitrusTest
    public void testTodoServiceReplication() {
        $(timer()
            .timerId("createTodoItems")
            .fork(true)
            .delay(500L)
            .interval(1000L)
            .repeatCount(5)
            .actions(
                createVariable("todoId", "citrus:randomUUID()"),
                createVariable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))"),
                createVariable("todoDescription", "Description: ${todoName}"),
                createVariable("done", "false"),
                http()
                    .client(todoClient)
                    .send()
                    .post("/api/todolist")
                    .message()
                    .type(MessageType.JSON)
                    .contentType(ContentType.APPLICATION_JSON.getMimeType())
                    .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"),
                http()
                    .client(todoClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .message()
                    .type(MessageType.PLAINTEXT)
                    .body("${todoId}")
            ));

        $(kubernetes()
            .pods()
            .list()
            .label("app=todo")
            .validate((pods, context) -> {
                Assert.assertNotNull(pods.getResult());
                Assert.assertEquals(pods.getResult().getItems().size(), 1L);
                context.setVariable("todoPod", pods.getResult().getItems().get(0).getMetadata().getName());
            }));

        $(parallel()
            .actions(
                kubernetes()
                    .pods()
                    .watch()
                    .name("${todoPod}")
                    .namespace("default")
                    .validate((result, context) -> Assert.assertEquals(((WatchEventResult) result).getAction(), Watcher.Action.MODIFIED)),
                kubernetes()
                    .pods()
                    .delete("${todoPod}")
                    .namespace("default")
                    .validate((result, context) -> Assert.assertTrue(result.getResult().getSuccess()))
            ));

        $(sleep().milliseconds(2000L));

        $(stopTimer("createTodoItems"));

        createVariable("todoId", "citrus:randomUUID()");
        createVariable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        createVariable("todoDescription", "Description: ${todoName}");
        createVariable("done", "false");

        $(http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"));

        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .body("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }
}
