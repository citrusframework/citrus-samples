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

package com.consol.citrus.samples.javaee.employee;

import javax.ws.rs.core.MediaType;
import java.net.URL;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.samples.javaee.Deployments;
import com.consol.citrus.ws.server.WebServiceServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeSmsGatewayTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @CitrusEndpoint
    private WebServiceServer smsGatewayServer;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.employeeWebRegistry();
    }

    @Before
    public void setUp() throws Exception {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    @Test
    @CitrusTest
    public void testPostWithWelcomeEmail(@CitrusResource TestCaseRunner citrus) {
        citrus.variable("employee.name", "Bernadette");
        citrus.variable("employee.age", "24");
        citrus.variable("employee.mobile", "4915199999999");

        citrus.run(http()
            .client(serviceUri)
            .send()
            .post()
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .payload("name=${employee.name}&age=${employee.age}&mobile=${employee.mobile}"));

        citrus.run(receive()
            .endpoint(smsGatewayServer)
            .payload(new ClassPathResource("templates/send-sms-request.xml")));

        citrus.run(send()
            .endpoint(smsGatewayServer)
                .payload(new ClassPathResource("templates/send-sms-response.xml")));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.run(http()
            .client(serviceUri)
            .send()
            .get()
            .accept(MediaType.APPLICATION_XML));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.OK)
            .payload("<employees>" +
                       "<employee>" +
                         "<age>${employee.age}</age>" +
                         "<name>${employee.name}</name>" +
                         "<mobile>${employee.mobile}</mobile>" +
                       "</employee>" +
                     "</employees>"));
    }
}
