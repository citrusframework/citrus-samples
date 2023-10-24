/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.samples.bookstore;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
@Test
public class AddBook_Ok_1_IT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @CitrusTest(name = "AddBook_Ok_1_IT")
    public void addBook_OK_1_Test() {
        description("This test shows basic SOAP WebService client server communication. " +
                "Citrus sends a SOAP request in order to add a book to the registry. As client Citrus receives the SOAP " +
                "response and validates the message content.");

        variable("isbn", "978-0596517335");

        $(soap()
           .client(bookStoreClient)
           .send()
           .message()
           .soapAction("addBook")
           .body("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                       "<bkr:book>" +
                           "<bkr:title>Maven: The Definitive Guide</bkr:title>" +
                           "<bkr:author>Mike Loukides, Sonatype</bkr:author>" +
                           "<bkr:isbn>${isbn}</bkr:isbn>" +
                           "<bkr:year>2008</bkr:year>" +
                       "</bkr:book>" +
                   "</bkr:AddBookRequestMessage>"));

        $(soap()
            .client(bookStoreClient)
            .receive()
            .message()
            .body("<bkr:AddBookResponseMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:success>true</bkr:success>" +
                    "</bkr:AddBookResponseMessage>"));
    }

}
