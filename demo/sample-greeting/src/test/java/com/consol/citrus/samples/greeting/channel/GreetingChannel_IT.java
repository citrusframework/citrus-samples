/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.samples.greeting.channel;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
public class GreetingChannel_IT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("greetingsEndpoint")
    private ChannelEndpoint greetingsEndpoint;

    @Autowired
    @Qualifier("greetingsTransformedEndpoint")
    private ChannelEndpoint greetingsTransformedEndpoint;

    @Test
    @CitrusTest(name = "GreetingChannel_IT")
    public void greetingChannel_IT() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        $(send()
            .description("Send asynchronous greeting request: Citrus -> GreetingService")
            .endpoint(greetingsEndpoint)
            .message()
            .body("<tns:GreetingRequestMessage xmlns:tns=\"http://www.citrusframework.org/samples/greeting\">\n" +
                        "<tns:CorrelationId>${correlationId}</tns:CorrelationId>\n" +
                        "<tns:Operation>sayHello</tns:Operation>\n" +
                        "<tns:User>${user}</tns:User>\n" +
                        "<tns:Text>Hello Citrus!</tns:Text>\n" +
                    "</tns:GreetingRequestMessage>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));

        $(receive()
            .description("Receive asynchronous greeting response: GreetingService -> Citrus")
            .endpoint(greetingsTransformedEndpoint)
            .message()
            .body("<tns:GreetingResponseMessage xmlns:tns=\"http://www.citrusframework.org/samples/greeting\">\n" +
                        "<tns:CorrelationId>${correlationId}</tns:CorrelationId>\n" +
                        "<tns:Operation>sayHello</tns:Operation>\n" +
                        "<tns:User>GreetingService</tns:User>\n" +
                        "<tns:Text>Hello ${user}!</tns:Text>\n" +
                    "</tns:GreetingResponseMessage>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}"));
    }
}
