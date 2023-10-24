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

package com.consol.citrus.samples.gradle;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = { EndpointConfig.class })
public class MessagingTest extends TestNGCitrusSpringSupport {

    @Autowired
    private DirectEndpoint testEndpoint;

    @Test
    @CitrusTest
    public void testMessaging() {
        $(echo("Test simple message send and receive"));

        $(send()
            .endpoint(testEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Hello Citrus!"));

        $(receive()
            .endpoint(testEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Hello Citrus!"));

        $(echo("Successful send and receive"));
    }
}
