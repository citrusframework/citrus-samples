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
import com.consol.citrus.config.CitrusBaseConfig;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

@RunWith(Arquillian.class)
public class EmployeeResourceTest {

    private Citrus citrusFramework = Citrus.newInstance(CitrusBaseConfig.class);
    private CitrusTestBuilder citrus;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        RegistryApplication.class, EmployeeResource.class, Employees.class,
                        Employee.class, EmployeeRepository.class);
    }

    @ArquillianResource
    private URL baseUri;
    private String serviceUri;


    @Before
    public void setUp() throws MalformedURLException {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
        citrus = new CitrusTestBuilder(citrusFramework.getApplicationContext());
    }

    /**
     * Test adding new employees and getting list of all employees.
     */
    @Test @InSequence(1)
    public void testPostAndGet() {
        citrus.name("EmployeePostAndGetTest");

        citrus.send(serviceUri)
                .message(new HttpMessage("name=Penny&age=20")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage("name=Leonard&age=21")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage("name=Sheldon&age=22")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
                .message(new HttpMessage("<employees>" +
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
                        "</employees>")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

    @Test @InSequence(2)
    public void testGetSingle() {
        citrus.name("EmployeeGetSingleTest");

        citrus.send(serviceUri + "/1")
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri + "/1")
                .message(new HttpMessage("<employee>" +
                        "<age>21</age>" +
                        "<name>Leonard</name>" +
                        "</employee>")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

    @Test @InSequence(3)
    public void testPut() {
        citrus.name("EmployeePutTest");

        citrus.send(serviceUri)
                .message(new HttpMessage("name=Howard&age=21")
                        .method(HttpMethod.PUT)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
                .message(new HttpMessage("<employees>" +
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
                            "</employee>" +
                        "</employees>")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

    @Test @InSequence(4)
    public void testDelete() {
        citrus.name("EmployeeDeleteTest");

        citrus.send(serviceUri + "/Leonard")
                .message(new HttpMessage()
                        .method(HttpMethod.DELETE));

        citrus.receive(serviceUri + "/Leonard")
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
                .message(new HttpMessage("<employees>" +
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
                            "</employee>" +
                        "</employees>")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

    @Test @InSequence(5)
    public void testClientSideNegotiation() {
        citrus.name("EmployeeClientSideNegotiationTest");

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_JSON));

        citrus.receive(serviceUri)
                .messageType(MessageType.JSON)
                .message(new HttpMessage("{\"employee\":[" +
                            "{\"name\":\"Penny\",\"age\":20}," +
                            "{\"name\":\"Sheldon\",\"age\":22}," +
                            "{\"name\":\"Howard\",\"age\":21}" +
                        "]}")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

    @Test @InSequence(6)
    public void testDeleteAll() {
        citrus.name("EmployeeDeleteAllTest");

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.DELETE));

        citrus.receive(serviceUri)
                .message(new HttpMessage()
                        .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
                .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
                .message(new HttpMessage("<employees></employees>")
                        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

}