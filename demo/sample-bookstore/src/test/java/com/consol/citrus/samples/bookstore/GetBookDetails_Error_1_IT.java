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

import static org.citrusframework.ws.actions.AssertSoapFault.Builder.assertSoapFault;
import static org.citrusframework.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class GetBookDetails_Error_1_IT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @Test
    @CitrusTest(name = "GetBookDetails_Error_1_IT")
    public void getBookDetails_Error_1_IT() {
        description("This test forces a SOAP fault in WebService response. Citrus asks for book details regarding a non-existent book." +
                "SOAP WebService server creates a SOAP fault that is validated in Citrus.");

        variable("isbn", "000-0000000000");
        variable("faultCode", "{http://www.consol.com/citrus/samples/errorcodes}CITRUS:1002");

        $(assertSoapFault()
                .faultCode("${faultCode}")
                .faultString("Book(isbn:'${isbn}') not available in registry")
                .when(
                    soap()
                        .client(bookStoreClient)
                        .send()
                        .message()
                        .soapAction("getBookDetails")
                        .body("<bkr:GetBookDetailsRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                                    "<bkr:isbn>${isbn}</bkr:isbn>" +
                                "</bkr:GetBookDetailsRequestMessage>"))
                );
    }

}
