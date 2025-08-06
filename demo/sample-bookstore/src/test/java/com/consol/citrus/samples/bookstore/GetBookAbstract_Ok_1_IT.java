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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class GetBookAbstract_Ok_1_IT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @CitrusTest(name = "GetBookAbstract_Ok_1_IT")
    public void getBookAbstract_Ok_1_IT() {
        description("This test shows basic SOAP WebService client server communication. " +
                "Citrus first of all adds a book to the registry in order to request for the book " +
                "details right after book was added. This time the book abstract is also returned " +
                "as SOAP attachment.");

        variable("isbn", "978-1933988999");

        $(soap()
            .client(bookStoreClient)
            .send()
            .message()
            .soapAction("addBook")
            .body("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:title>Spring in Action</bkr:title>" +
                            "<bkr:author>Craig Walls, Ryan Breidenbach</bkr:author>" +
                            "<bkr:isbn>${isbn}</bkr:isbn>" +
                            "<bkr:year>2007</bkr:year>" +
                        "</bkr:book>" +
                    "</bkr:AddBookRequestMessage>"));

        $(soap()
            .client(bookStoreClient)
            .receive()
            .message()
            .body("<bkr:AddBookResponseMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:success>true</bkr:success>" +
                    "</bkr:AddBookResponseMessage>"));

        $(soap()
            .client(bookStoreClient)
            .send()
            .message()
            .soapAction("getBookAbstract")
            .body("<bkr:GetBookAbstractRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:isbn>${isbn}</bkr:isbn>" +
                    "</bkr:GetBookAbstractRequestMessage>"));

        $(soap()
            .client(bookStoreClient)
            .receive()
            .message()
            .body("<bkr:GetBookAbstractResponseMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:id>@ignore@</bkr:id>" +
                            "<bkr:title>Spring in Action</bkr:title>" +
                            "<bkr:author>Craig Walls, Ryan Breidenbach</bkr:author>" +
                            "<bkr:isbn>${isbn}</bkr:isbn>" +
                            "<bkr:year>2007</bkr:year>" +
                            "<bkr:registration-date>@ignore@</bkr:registration-date>" +
                        "</bkr:book>" +
                    "</bkr:GetBookAbstractResponseMessage>")
            .attachment("book-abstract", "text/plain", Resources.fromClasspath("book-abstract.txt", CitrusEndpointConfig.class))
            .extract(extractor().xpath()
                        .expression("/bkr:GetBookAbstractResponseMessage/bkr:book/bkr:id", "bookId")));
    }

}
