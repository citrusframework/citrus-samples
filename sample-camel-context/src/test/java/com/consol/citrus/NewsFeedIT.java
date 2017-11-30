/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class NewsFeedIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JmsEndpoint newsJmsEndpoint;

    @Autowired
    private WebServiceServer newsServer;

    @CitrusTest(name = "NewsFeed_Ok_1_IT")
    public void newsFeed_Ok_1_Test() {
        send(newsJmsEndpoint)
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>");

        receive(newsServer)
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>")
                .header(SoapMessageHeaders.SOAP_ACTION, "newsFeed");

        send(newsServer)
                .header(SoapMessageHeaders.HTTP_STATUS_CODE, "200");
    }

    @CitrusTest(name = "NewsFeed_Ok_2_IT")
    public void newsFeed_Ok_2_Test() {
        echo("Send JMS request message to queue destination");

        send("newsJmsEndpoint")
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>")
                .header("Operation", "HelloService/sayHello");

        echo("Receive JMS message on queue destination");

        soap().server(newsServer)
                .receive()
                .soapAction( "newsFeed")
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>");

        soap().server(newsServer)
                .send()
                .message(new SoapMessage()
                        .setHeader(SoapMessageHeaders.HTTP_STATUS_CODE, "200"));
    }

}
