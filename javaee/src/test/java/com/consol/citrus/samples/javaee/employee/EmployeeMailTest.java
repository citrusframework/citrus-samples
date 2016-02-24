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

package com.consol.citrus.samples.javaee.employee;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.*;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import com.consol.citrus.samples.javaee.mail.MailService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.net.URL;

@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeMailTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @CitrusEndpoint
    private MailServer mailServer;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        RegistryApplication.class, MailService.class, EmployeeResource.class, Employees.class,
                        Employee.class, EmployeeRepository.class);
    }

    @Before
    public void setUp() throws Exception {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    /**
     * Test adding new employees and getting list of all employees.
     */
    @Test
    @CitrusTest
    public void testPostWithWelcomeEmail(@CitrusResource TestDesigner citrus) {
        citrus.send(serviceUri)
                .fork(true)
                .message(new HttpMessage("name=Rajesh&age=20&email=rajesh@example.com")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(mailServer)
                .payload("<mail-message xmlns=\"http://www.citrusframework.org/schema/mail/message\">" +
                            "<from>employee-registry@example.com</from>" +
                            "<to>rajesh@example.com</to>" +
                            "<cc></cc>" +
                            "<bcc></bcc>" +
                            "<subject>Welcome new employee</subject>" +
                            "<body>" +
                                "<contentType>text/plain; charset=us-ascii</contentType>" +
                                "<content>We welcome you 'Rajesh' to our company - now get to work!</content>" +
                            "</body>" +
                        "</mail-message>")
                .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Welcome new employee")
                .header(CitrusMailMessageHeaders.MAIL_FROM, "employee-registry@example.com")
                .header(CitrusMailMessageHeaders.MAIL_TO, "rajesh@example.com");

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .status(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
                .message(new HttpMessage("<employees>" +
                            "<employee>" +
                                "<age>20</age>" +
                                "<name>Rajesh</name>" +
                                "<email>rajesh@example.com</email>" +
                            "</employee>" +
                        "</employees>")
                        .status(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

}