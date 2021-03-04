/*
 * Copyright 2006-2012 the original author or authors.
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

import java.util.Calendar;
import java.util.Map;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.samples.bookstore.model.AddBookRequestMessage;
import com.consol.citrus.samples.bookstore.model.AddBookResponseMessage;
import com.consol.citrus.samples.bookstore.model.Book;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.validation.xml.XmlMarshallingValidationProcessor;
import com.consol.citrus.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.message.builder.MarshallingPayloadBuilder.Builder.marshal;

/**
 * @author Christoph Deppisch
 */
@Test
public class AddBook_Ok_2_IT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("bookStoreClient")
    private WebServiceClient bookStoreClient;

    @Autowired
    private Marshaller marshaller;

    @CitrusTest(name = "AddBook_Ok_2_IT")
    public void addBookTest() {
        String isbn = "978-citrus:randomNumber(10)";

        $(send()
            .endpoint(bookStoreClient)
            .message()
            .body(marshal(createAddBookRequestMessage(isbn), marshaller))
            .header("citrus_soap_action", "addBook"));

        $(receive()
            .endpoint(bookStoreClient)
            .validate(new XmlMarshallingValidationProcessor<AddBookResponseMessage>() {
                @Override
                public void validate(AddBookResponseMessage response, Map<String, Object> headers, TestContext context) {
                    Assert.isTrue(response.isSuccess(), "Unexpected add book response");
                }
            }));
    }

    /**
     * @param isbn
     * @return
     */
    private AddBookRequestMessage createAddBookRequestMessage(String isbn) {
        AddBookRequestMessage requestMessage = new AddBookRequestMessage();
        Book book = new Book();
        book.setAuthor("Mike Loukides, Sonatype");
        book.setTitle("Maven: The Definitive Guide");
        book.setIsbn(isbn);
        book.setYear(2008);
        book.setRegistrationDate(Calendar.getInstance());
        requestMessage.setBook(book);
        return requestMessage;
    }

}
