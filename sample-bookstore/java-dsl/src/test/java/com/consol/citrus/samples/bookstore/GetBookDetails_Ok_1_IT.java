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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GetBookDetails_Ok_1_IT extends TestNGCitrusTestDesigner {

    @Autowired
    private WebServiceClient bookStoreClient;

    @Test
    @CitrusTest(name = "GetBookDetails_Ok_1_IT")
    public void getBookDetails_Ok_1_IT() {
        description("This test shows basic SOAP WebService client server communication. " +
                "Citrus first of all adds a book to the registry in order to request for the book " +
                "details right after book was added.");

        variable("isbn", "978-1933988139");

        soap()
            .client(bookStoreClient)
            .send()
            .soapAction("addBook")
            .payload("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:title>Maven: The Definitive Guide</bkr:title>" +
                            "<bkr:author>Mike Loukides, Sonatype</bkr:author>" +
                            "<bkr:isbn>${isbn}</bkr:isbn>" +
                            "<bkr:year>2008</bkr:year>" +
                        "</bkr:book>" +
                    "</bkr:AddBookRequestMessage>");

        soap()
            .client(bookStoreClient)
            .receive()
            .payload("<bkr:AddBookResponseMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:success>true</bkr:success>" +
                    "</bkr:AddBookResponseMessage>");

        soap()
            .client(bookStoreClient)
            .send()
            .soapAction("getBookDetails")
            .payload("<bkr:GetBookDetailsRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:isbn>${isbn}</bkr:isbn>" +
                    "</bkr:GetBookDetailsRequestMessage>");

        soap()
            .client(bookStoreClient)
            .receive()
            .payload("<bkr:GetBookDetailsResponseMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:id>?</bkr:id>" +
                            "<bkr:title>Maven: The Definitive Guide</bkr:title>" +
                            "<bkr:author>Mike Loukides, Sonatype</bkr:author>" +
                            "<bkr:isbn>${isbn}</bkr:isbn>" +
                            "<bkr:year>2008</bkr:year>" +
                            "<bkr:registration-date>?</bkr:registration-date>" +
                        "</bkr:book>" +
                    "</bkr:GetBookDetailsResponseMessage>")
            .ignore("/bkr:GetBookDetailsResponseMessage/bkr:book/bkr:id")
            .ignore("/bkr:GetBookDetailsResponseMessage/bkr:book/bkr:registration-date")
            .extractFromPayload("/bkr:GetBookDetailsResponseMessage/bkr:book/bkr:id", "bookId");
    }

}
