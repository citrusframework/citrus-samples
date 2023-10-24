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

import static org.citrusframework.validation.script.ScriptValidationContext.Builder.groovy;
import static org.citrusframework.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class ListBooks_Ok_2_IT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @Test
    @CitrusTest(name = "ListBooks_Ok_2_IT")
    public void listBooks_Ok_2_IT() {
        description("In this test we get the list of all available books. This time we validate the existence of a newly added book via Groovy script.");

        variable("isbn", "citrus:concat('978-', citrus:randomNumber(9, true))");
        variable("year", "citrus:currentDate('yyyy')");

        $(soap()
            .client(bookStoreClient)
            .send()
            .message()
            .soapAction("addBook")
            .body("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                        "<bkr:book>" +
                            "<bkr:title>Random Book</bkr:title>\n" +
                            "<bkr:author>Random Author</bkr:author>\n" +
                            "<bkr:isbn>${isbn}</bkr:isbn>\n" +
                            "<bkr:year>${year}</bkr:year>" +
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
            .validate(groovy()
                    .script("org.testng.Assert.assertTrue(root.books.book.findAll{ it.isbn == '${isbn}' }.size() == 1, " +
                            "\"Missing book with isbn: '${isbn}' in book list!\")")));
    }

}
