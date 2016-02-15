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
import com.consol.citrus.annotations.*;
import com.consol.citrus.arquillian.shrinkwrap.CitrusArchiveBuilder;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpointConfiguration;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.config.CitrusConfig;
import com.consol.citrus.samples.javaee.employee.EmployeeRepository;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import com.consol.citrus.samples.javaee.mail.MailSessionBean;
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
    public static WebArchive createDeployment() throws MalformedURLException {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(MailSessionBean.class, EmployeeJmsResource.class, Employees.class,
                        Employee.class, EmployeeRepository.class, CitrusConfig.class)
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
