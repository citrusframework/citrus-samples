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
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static com.consol.citrus.dsl.XpathSupport.xpath;
import static com.consol.citrus.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class ListBooks_Ok_1_IT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @Test
    @CitrusTest(name = "ListBooks_Ok_1_IT")
    public void listBooks_Ok_1_IT() {
        description("In this test we add a book first to the registry and afterwards try to get the list of all available books. " +
                "The newly added book has to be present in this list. As we do not know exactly what other books might be in the " +
                "complete list of books we have to validate with XPath magic.");

        variable("isbn", "978-0321200686");


        $(soap()
            .client(bookStoreClient)
            .send()
            .message()
            .soapAction("addBook")
            .body("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:title>Enterprise Integration Patterns: Designing, Building, and Deploying Messaging Solutions</bkr:title>" +
                            "<bkr:author>Gregor Hohpe, Bobby Wolf</bkr:author>" +
                            "<bkr:isbn>${isbn}</bkr:isbn>" +
                            "<bkr:year>2003</bkr:year>" +
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
            .soapAction("listBooks")
            .body("<bkr:ListBooksRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\"/>"));

        $(soap()
            .client(bookStoreClient)
            .receive()
            .validate(xpath()
                        .expression("boolean:count(/bkr:ListBooksResponseMessage/bkr:books/bkr:book) > 0", true)
                        .expression("boolean:/bkr:ListBooksResponseMessage/bkr:books/bkr:book/bkr:isbn[.='${isbn}']", true)));
    }

}
