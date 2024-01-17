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

import java.io.IOException;
import java.net.URI;

import jakarta.inject.Singleton;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class ChatService {

    private static final Logger LOG = Logger.getLogger(ChatService.class);

    @ConfigProperty(name = "chat.websocket.uri", defaultValue = "http://localhost:8088/chat")
    URI uri;

    private Session session;

    public void send(String user, String message) {
        openSession().getAsyncRemote().sendText(">> %s: %s".formatted(user, message));
    }

    private Session openSession() {
        if (session == null) {
            try {
                session = ContainerProvider.getWebSocketContainer().connectToServer(ChatClient.class, uri);
            } catch (DeploymentException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return session;
    }

    @ClientEndpoint
    public static class ChatClient {

        @OnOpen
        public void open(Session session) {
            LOG.info("CONNECTED!");
            session.getAsyncRemote().sendText("Quarkus wants to join ...");
        }

        @OnMessage
        void message(String msg) {
            LOG.info(msg);
        }

    }
}
