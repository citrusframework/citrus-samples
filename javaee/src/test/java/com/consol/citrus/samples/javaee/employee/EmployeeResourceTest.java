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
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import com.consol.citrus.samples.javaee.mail.MailService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeResourceTest {

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
    public void setUp() throws MalformedURLException {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    @Test
    @InSequence(1)
    @CitrusTest
    public void testPostAndGet(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .payload("name=Penny&age=20");

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .payload("name=Leonard&age=21");

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .payload("name=Sheldon&age=22");

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .get()
                .accept(MediaType.APPLICATION_XML);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .payload("<employees>" +
                            "<employee>" +
                                "<age>20</age>" +
                                "<name>Penny</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>21</age>" +
                                "<name>Leonard</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>22</age>" +
                                "<name>Sheldon</name>" +
                            "</employee>" +
                        "</employees>");

        citrusFramework.run(citrus.getTestCase());
    }

    @Test
    @InSequence(2)
    @CitrusTest
    public void testGetSingle(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .get("/1")
                .accept(MediaType.APPLICATION_XML);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .payload("<employee>" +
                            "<age>21</age>" +
                            "<name>Leonard</name>" +
                        "</employee>");

        citrusFramework.run(citrus.getTestCase());
    }

    @Test
    @InSequence(3)
    @CitrusTest
    public void testPut(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .put()
                .fork(true)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .payload("name=Howard&age=21&email=howard@example.com");

        citrus.receive(mailServer)
                .payload("<mail-message xmlns=\"http://www.citrusframework.org/schema/mail/message\">" +
                            "<from>employee-registry@example.com</from>" +
                            "<to>howard@example.com</to>" +
                            "<cc></cc>" +
                            "<bcc></bcc>" +
                            "<subject>Welcome new employee</subject>" +
                            "<body>" +
                                "<contentType>text/plain; charset=us-ascii</contentType>" +
                                "<content>We welcome you 'Howard' to our company - now get to work!</content>" +
                            "</body>" +
                        "</mail-message>")
                .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Welcome new employee")
                .header(CitrusMailMessageHeaders.MAIL_FROM, "employee-registry@example.com")
                .header(CitrusMailMessageHeaders.MAIL_TO, "howard@example.com");

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .get()
                .accept(MediaType.APPLICATION_XML);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .payload("<employees>" +
                            "<employee>" +
                                "<age>20</age>" +
                                "<name>Penny</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>21</age>" +
                                "<name>Leonard</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>22</age>" +
                                "<name>Sheldon</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>21</age>" +
                                "<name>Howard</name>" +
                                "<email>howard@example.com</email>" +
                            "</employee>" +
                        "</employees>");

        citrusFramework.run(citrus.getTestCase());
    }

    @Test
    @InSequence(4)
    @CitrusTest
    public void testDelete(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .delete("/Leonard");

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .get()
                .accept(MediaType.APPLICATION_XML);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .payload("<employees>" +
                            "<employee>" +
                                "<age>20</age>" +
                                "<name>Penny</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>22</age>" +
                                "<name>Sheldon</name>" +
                            "</employee>" +
                            "<employee>" +
                                "<age>21</age>" +
                                "<name>Howard</name>" +
                                "<email>howard@example.com</email>" +
                            "</employee>" +
                        "</employees>");

        citrusFramework.run(citrus.getTestCase());
    }

    @Test
    @InSequence(5)
    @CitrusTest
    public void testClientSideNegotiation(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .get()
                .accept(MediaType.APPLICATION_JSON);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .payload("{\"employee\":[" +
                            "{\"name\":\"Penny\",\"age\":20,\"email\":null}," +
                            "{\"name\":\"Sheldon\",\"age\":22,\"email\":null}," +
                            "{\"name\":\"Howard\",\"age\":21,\"email\":\"howard@example.com\"}" +
                        "]}");

        citrusFramework.run(citrus.getTestCase());
    }

    @Test
    @InSequence(6)
    @CitrusTest
    public void testDeleteAll(@CitrusResource TestDesigner citrus) {
        citrus.http().client(serviceUri)
                .delete();

        citrus.http().client(serviceUri)
                .response(HttpStatus.NO_CONTENT);

        citrus.http().client(serviceUri)
                .get()
                .accept(MediaType.APPLICATION_XML);

        citrus.http().client(serviceUri)
                .response(HttpStatus.OK)
                .payload("<employees></employees>");

        citrusFramework.run(citrus.getTestCase());
    }

}