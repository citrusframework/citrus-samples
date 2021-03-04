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

import static com.consol.citrus.ws.actions.AssertSoapFault.Builder.assertSoapFault;
import static com.consol.citrus.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class SchemaValidation_Error_1_IT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient bookStoreClient;

    @Test
    @CitrusTest(name = "SchemaValidation_Error_1_IT")
    public void schemaValidation_Error_1_IT() {
        description("This test gets schema validation errors in SOAP fault response from server as the request is not " +
                "valid ('year'=>'03.Okt.2008' not a valid number).");

        variable("isbn", "978-0596517335");
        variable("faultCode", "{http://www.consol.com/citrus/samples/errorcodes}CITRUS:999");

        $(assertSoapFault()
                .faultCode("${faultCode}")
                .faultString("Client sent invalid request!")
                .when(
                    soap()
                        .client(bookStoreClient)
                        .send()
                        .message()
                        .soapAction("addBook")
                        .body("<bkr:AddBookRequestMessage xmlns:bkr=\"http://www.consol.com/schemas/bookstore\">" +
                                    "<bkr:book>" +
                                        "<bkr:title>Maven: The Definitive Guide</bkr:title>" +
                                        "<bkr:author>Mike Loukides, Sonatype</bkr:author>" +
                                        "<bkr:isbn>${isbn}</bkr:isbn>" +
                                        "<bkr:year>03.Okt.2008</bkr:year>" +
                                    "</bkr:book>" +
                                "</bkr:AddBookRequestMessage>"))
                );
    }

}
