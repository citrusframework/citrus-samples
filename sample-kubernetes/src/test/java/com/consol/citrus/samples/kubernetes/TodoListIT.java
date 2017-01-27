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
import com.consol.citrus.message.MessageType;
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
        kubernetes()
            .client(k8sClient)
            .pods()
            .list()
            .label("app=todo")
            .validate("$..status.phase", "Running")
            .validate((pods, context) -> {
                Assert.assertFalse(CollectionUtils.isEmpty(pods.getResult().getItems()));
            });

        kubernetes()
            .client(k8sClient)
            .services()
            .get("todo-app")
            .validate((service, context) -> {
                Assert.assertNotNull(service.getResult());
            });
    }

    @Test
    @CitrusTest
    public void testTodoService() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/todo/${todoId}")
            .accept("application/json");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
    }

    @Test
    @CitrusTest
    public void testTodoServiceReplication() {
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
                http()
                    .client(todoClient)
                    .send()
                    .post("/todolist")
                    .messageType(MessageType.JSON)
                    .contentType("application/json")
                    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}"),

                http()
                    .client(todoClient)
                    .receive()
                    .response(HttpStatus.OK)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("${todoId}")
            );

        kubernetes()
            .pods()
            .list()
            .label("app=todo")
            .validate((pods, context) -> {
                Assert.assertNotNull(pods.getResult());
                Assert.assertEquals(pods.getResult().getItems().size(), 1L);

                context.setVariable("todoPod", pods.getResult().getItems().get(0).getMetadata().getName());
            });

        kubernetes()
            .pods()
            .delete("${todoPod}")
            .validate((result, context) -> Assert.assertTrue(result.getResult().getSuccess()));

        sleep(2000L);

        stopTimer("createTodoItems");

        createVariable("todoId", "citrus:randomUUID()");
        createVariable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        createVariable("todoDescription", "Description: ${todoName}");

        http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/todo/${todoId}")
            .accept("application/json");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
    }
}
