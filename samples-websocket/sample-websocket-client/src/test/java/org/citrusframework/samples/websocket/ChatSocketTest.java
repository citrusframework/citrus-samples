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

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.websocket.client.WebSocketClient;
import org.citrusframework.websocket.client.WebSocketClientBuilder;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration( classes = { ChatSocketTest.EndpointConfig.class } )
class ChatSocketTest {

    @CitrusResource
    TestCaseRunner t;

    @CitrusEndpoint
    WebSocketClient chatClient;

    @Test
    void shouldConnectAndSendMessage() {
        t.given(send()
                .endpoint(chatClient)
                .message()
                .body("Hello Quarkus chat!"));

        t.then(receive()
                .endpoint(chatClient)
                .message()
                .body(">> citrus: Hello Quarkus chat!"));
    }

    public static class EndpointConfig {

        @BindToRegistry
        public WebSocketClient chatClient() {
            return new WebSocketClientBuilder()
                    .requestUrl("ws://localhost:8081/chat/citrus")
                    .build();
        }
    }
}
