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

package com.consol.citrus.samples.javaee.employee.jms;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.*;
import com.consol.citrus.arquillian.shrinkwrap.CitrusArchiveBuilder;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpointConfiguration;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.Deployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.connection.SingleConnectionFactory;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.io.IOException;

@RunWith(Arquillian.class)
public class EmployeeJmsTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @Resource(mappedName = "jms/queue/employee")
    private Queue employeeQueue;

    @Resource(mappedName = "/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private JmsSyncEndpoint employeeJmsEndpoint;

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static WebArchive createDeployment() throws IOException {
        return Deployments.employeeJmsRegistry()
                    .addAsResource(new ClassPathResource("wsdl/SmsGateway.wsdl").getFile(), new BasicPath("/wsdl/SmsGateway.wsdl"))
                    .addAsLibraries(CitrusArchiveBuilder.latestVersion().core().javaDsl().mail().jms().build());
    }

    @Before
    public void setUp() {
        JmsSyncEndpointConfiguration endpointConfiguration = new JmsSyncEndpointConfiguration();
        endpointConfiguration.setConnectionFactory(new SingleConnectionFactory(connectionFactory));
        endpointConfiguration.setDestination(employeeQueue);
        employeeJmsEndpoint = new JmsSyncEndpoint(endpointConfiguration);
    }

    @After
    public void cleanUp() {
        closeConnections();
    }

    @Test
    @CitrusTest
    public void testAdd(@CitrusResource TestDesigner citrus) throws Exception {
        citrus.send(employeeJmsEndpoint)
                .messageType(MessageType.PLAINTEXT)
                .header("name", "Amy")
                .header("age", 20);

        citrus.receive(employeeJmsEndpoint)
                .messageType(MessageType.PLAINTEXT)
                .payload("Successfully created employee: Amy(20)")
                .header("success", true);

        citrusFramework.run(citrus.getTestCase());
    }

    private void closeConnections() {
        ((SingleConnectionFactory) employeeJmsEndpoint.getEndpointConfiguration().getConnectionFactory()).destroy();
    }
}
