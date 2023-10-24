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

package com.consol.citrus.samples.todolist;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private HttpClient todoBasicAuthClient;

    @Test
    @CitrusTest
    public void testBasicAuth() {
        $(http()
            .client(todoBasicAuthClient)
            .send()
            .get("/todo/")
            .message()
            .accept(ContentType.APPLICATION_XML.getMimeType()));

        $(http()
            .client(todoBasicAuthClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testBasicAuthHeader() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todo/")
            .message()
            .accept(ContentType.APPLICATION_XML.getMimeType())
            .header("Authorization", "Basic citrus:encodeBase64('citrus:secr3t')"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testBasicAuthMissing() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todo/")
            .message()
            .accept(ContentType.APPLICATION_XML.getMimeType()));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @CitrusTest
    public void testBasicAuthFailed() {
        $(http()
            .client(todoClient)
            .send()
            .get("/todo/")
            .message()
            .accept(ContentType.APPLICATION_XML.getMimeType())
            .header("Authorization", "Basic citrus:encodeBase64('wrong:wrong')"));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.UNAUTHORIZED));
    }
}
