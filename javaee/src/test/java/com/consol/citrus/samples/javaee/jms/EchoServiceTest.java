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

package com.consol.citrus.samples.javaee.jms;

import com.consol.citrus.Citrus;
import com.consol.citrus.arquillian.annotation.InjectCitrus;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpointConfiguration;
import com.consol.citrus.jms.message.JmsMessage;
import com.consol.citrus.message.MessageType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.jms.connection.SingleConnectionFactory;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.net.MalformedURLException;

@RunWith(Arquillian.class)
public class EchoServiceTest {

    @InjectCitrus
    private Citrus citrusFramework;

    @Resource(mappedName = "jms/queue/test")
    private Queue echoQueue;

    @Resource(mappedName = "/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private JmsSyncEndpoint jmsSyncEndpoint;

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static WebArchive createDeployment() throws MalformedURLException {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(EchoService.class);
    }

    @Before
    public void setUp() {
        JmsSyncEndpointConfiguration endpointConfiguration = new JmsSyncEndpointConfiguration();
        endpointConfiguration.setConnectionFactory(new SingleConnectionFactory(connectionFactory));
        endpointConfiguration.setDestination(echoQueue);
        jmsSyncEndpoint = new JmsSyncEndpoint(endpointConfiguration);
    }

    @After
    public void cleanUp() {
        closeConnections();
    }

    @Test
    public void shouldBeAbleToSendMessage(@CitrusTest CitrusTestBuilder citrus) throws Exception {
        String messageBody = "ping";
        citrus.send(jmsSyncEndpoint)
                .messageType(MessageType.PLAINTEXT)
                .message(new JmsMessage(messageBody));

        citrus.receive(jmsSyncEndpoint)
                .messageType(MessageType.PLAINTEXT)
                .message(new JmsMessage(messageBody));

        citrusFramework.run(citrus.getTestCase());
    }

    private void closeConnections() {
        ((SingleConnectionFactory)jmsSyncEndpoint.getEndpointConfiguration().getConnectionFactory()).destroy();
    }
}
