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

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.Deployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeResourceTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.employeeWebRegistry();
    }

    @Before
    public void setUp() throws MalformedURLException {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    @Test
    @InSequence(1)
    @CitrusTest
    public void testPostAndGet(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .payload("name=Penny&age=20"));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.run(http()
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .payload("name=Leonard&age=21"));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.run(http()
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .payload("name=Sheldon&age=22"));

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
                      "</employees>"));
    }

    @Test
    @InSequence(2)
    @CitrusTest
    public void testGetSingle(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .get("/1")
            .accept(MediaType.APPLICATION_XML));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.OK)
            .payload("<employee>" +
                       "<age>21</age>" +
                       "<name>Leonard</name>" +
                     "</employee>"));
    }

    @Test
    @InSequence(3)
    @CitrusTest
    public void testPut(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .put()
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .payload("name=Howard&age=21&email=howard@example.com"));

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
                     "</employees>"));
    }

    @Test
    @InSequence(4)
    @CitrusTest
    public void testDelete(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .delete("/Leonard"));

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
                     "</employees>"));
    }

    @Test
    @InSequence(5)
    @CitrusTest
    public void testClientSideNegotiation(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .get()
            .accept(MediaType.APPLICATION_JSON));

        citrus.run(http()
            .client(serviceUri)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{\"employee\":[" +
                       "{\"name\":\"Penny\",\"age\":20,\"email\":null,\"mobile\":null}," +
                       "{\"name\":\"Sheldon\",\"age\":22,\"email\":null,\"mobile\":null}," +
                       "{\"name\":\"Howard\",\"age\":21,\"email\":\"howard@example.com\",\"mobile\":null}" +
                     "]}"));
    }

    @Test
    @InSequence(6)
    @CitrusTest
    public void testDeleteAll(@CitrusResource TestCaseRunner citrus) {
        citrus.run(http()
            .client(serviceUri)
            .send()
            .delete());

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
            .payload("<employees></employees>"));
    }

}
