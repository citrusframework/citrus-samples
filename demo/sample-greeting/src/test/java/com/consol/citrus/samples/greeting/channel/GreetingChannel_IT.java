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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GreetingChannel_IT extends TestNGCitrusTestRunner {

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

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(greetingsEndpoint)
            .payload("<tns:GreetingRequestMessage xmlns:tns=\"http://www.citrusframework.org/samples/greeting\">\n" +
                        "<tns:CorrelationId>${correlationId}</tns:CorrelationId>\n" +
                        "<tns:Operation>sayHello</tns:Operation>\n" +
                        "<tns:User>${user}</tns:User>\n" +
                        "<tns:Text>Hello Citrus!</tns:Text>\n" +
                    "</tns:GreetingRequestMessage>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}")
            .description("Send asynchronous greeting request: Citrus -> GreetingService"));

        receive(receiveMessageBuidler -> receiveMessageBuidler
            .endpoint(greetingsTransformedEndpoint)
            .payload("<tns:GreetingResponseMessage xmlns:tns=\"http://www.citrusframework.org/samples/greeting\">\n" +
                        "<tns:CorrelationId>${correlationId}</tns:CorrelationId>\n" +
                        "<tns:Operation>sayHello</tns:Operation>\n" +
                        "<tns:User>GreetingService</tns:User>\n" +
                        "<tns:Text>Hello ${user}!</tns:Text>\n" +
                    "</tns:GreetingResponseMessage>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}")
            .description("Receive asynchronous greeting response: GreetingService -> Citrus"));
    }
}