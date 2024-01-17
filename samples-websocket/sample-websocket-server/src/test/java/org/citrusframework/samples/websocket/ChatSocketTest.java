/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.samples.websocket;

import java.util.Collections;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.server.WebSocketServer;
import org.citrusframework.websocket.server.WebSocketServerBuilder;
import org.citrusframework.websocket.server.WebSocketServerEndpointConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = { ChatSocketTest.EndpointConfig.class })
class ChatSocketTest {

    @CitrusResource
    TestCaseRunner t;

    @CitrusEndpoint
    WebSocketEndpoint chatEndpoint;

    @Test
    void shouldBroadcastMessages() {
        t.when(http()
                .client("http://localhost:8081")
                .send()
                .post("chat/citrus-user")
                .fork(true)
                .message()
                .body("Hello from Citrus!"));

        t.then(receive()
                .endpoint(chatEndpoint)
                .message()
                .body("Quarkus wants to join ..."));

        t.then(send()
                .endpoint(chatEndpoint)
                .message()
                .body("Welcome Quarkus!"));

        t.then(receive()
                .endpoint(chatEndpoint)
                .message()
                .body(">> citrus-user: Hello from Citrus!"));

        t.then(http().client("http://localhost:8081")
                .receive()
                .response(HttpStatus.CREATED));
    }

    public static class EndpointConfig {

        private WebSocketEndpoint chatEndpoint;

        @BindToRegistry
        public WebSocketEndpoint chatEndpoint() {
            if (chatEndpoint == null) {
                WebSocketServerEndpointConfiguration chatEndpointConfig = new WebSocketServerEndpointConfiguration();
                chatEndpointConfig.setEndpointUri("/chat");
                chatEndpoint = new WebSocketEndpoint(chatEndpointConfig);
            }

            return chatEndpoint;
        }

        @BindToRegistry
        public WebSocketServer chatServer() {
            return new WebSocketServerBuilder()
                    .webSockets(Collections.singletonList(chatEndpoint()))
                    .port(8088)
                    .autoStart(true)
                    .build();
        }
    }
}
