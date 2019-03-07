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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.WatchEventResult;
import com.consol.citrus.message.MessageType;
import io.fabric8.kubernetes.client.Watcher;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        kubernetes(kubernetesActionBuilder -> kubernetesActionBuilder
            .client(k8sClient)
            .pods()
            .list()
            .label("app=todo")
            .validate("$..status.phase", "Running")
            .validate((pods, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(pods.getResult().getItems()));
            }));

        kubernetes(kubernetesActionBuilder -> kubernetesActionBuilder
            .client(k8sClient)
            .services()
            .get("citrus-sample-todo-service")
            .validate((service, context) -> {
                Assert.assertNotNull(service.getResult());
            }));
    }

    @Test
    @CitrusTest
    public void testTodoService() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @Test
    @CitrusTest
    public void testTodoServiceReplication() {
        // 8080
        // bricht irgendwie aus dem Schema aus: warum hat der timer nicht denselben Build-Prozess wie
        // der Rest?
        timer()
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
                http(httpActionBuilder -> httpActionBuilder
                    .client(todoClient)
                    .send()
                    .post("/api/todolist")
                    .messageType(MessageType.JSON)
                    .contentType(ContentType.APPLICATION_JSON.getMimeType())
                    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}")),

                http(httpActionBuilder -> httpActionBuilder
                    .client(todoClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("${todoId}"))
            );

        kubernetes(kubernetesActionBuilder -> kubernetesActionBuilder
            .pods()
            .list()
            .label("app=todo")
            .validate((pods, context) -> {
                Assert.assertNotNull(pods.getResult());
                Assert.assertEquals(pods.getResult().getItems().size(), 1L);
                context.setVariable("todoPod", pods.getResult().getItems().get(0).getMetadata().getName());
            }));

        // 8080
        // hier auch: warum anderer build-Prozess?
        parallel()
            .actions(
                kubernetes(kubernetesActionBuilder -> kubernetesActionBuilder
                    .pods()
                    .watch()
                    .name("${todoPod}")
                    .namespace("default")
                    .validate((result, context) -> Assert.assertEquals(((WatchEventResult) result).getAction(), Watcher.Action.MODIFIED))),
                kubernetes(kubernetesActionBuilder -> kubernetesActionBuilder
                    .pods()
                    .delete("${todoPod}")
                    .namespace("default")
                    .validate((result, context) -> Assert.assertTrue(result.getResult().getSuccess())))
            );

        sleep(2000L);

        stopTimer("createTodoItems");

        createVariable("todoId", "citrus:randomUUID()");
        createVariable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        createVariable("todoDescription", "Description: ${todoName}");
        createVariable("done", "false");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }
}
