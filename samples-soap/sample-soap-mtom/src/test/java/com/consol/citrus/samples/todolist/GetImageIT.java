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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.BinarySoapAttachmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GetImageIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private WebServiceClient imageClient;

    @Autowired
    private WebServiceServer imageServer;

    @Test
    @CitrusTest
    public void testGetImageMtom() {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("IMAGE");
        attachment.setContentType("image/png");
        attachment.setCharsetName("utf-8");
        attachment.setContentResourcePath("image/logo.png");

        $(soap()
            .client(imageClient)
            .send()
            .fork(true)
            .message()
            .soapAction("getImage")
            .body("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        $(soap()
            .server(imageServer)
            .receive()
            .message()
            .soapAction("getImage")
            .body("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        $(soap()
            .server(imageServer)
            .send()
            .message()
            .body("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>cid:IMAGE</image:image>" +
                    "</image:getImageResponse>")
            .attachment(attachment)
            .mtomEnabled(true));

        $(soap()
            .client(imageClient)
            .receive()
            .message()
            .validate(validation().xml()
                        .schemaValidation(false))
            .body("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>" +
                            "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:IMAGE\"/>" +
                        "</image:image>" +
                    "</image:getImageResponse>")
            .attachmentValidator(new BinarySoapAttachmentValidator())
            .attachment(attachment));
    }

    @Test
    @CitrusTest
    public void testGetImageMtomInline() {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("IMAGE");
        attachment.setContentType("image/png");
        attachment.setCharsetName("utf-8");
        attachment.setContentResourcePath("image/logo.png");
        attachment.setMtomInline(true);

        $(soap()
            .client(imageClient)
            .send()
            .fork(true)
            .message()
            .soapAction("getImage")
            .body("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        $(soap()
            .server(imageServer)
            .receive()
            .message()
            .soapAction("getImage")
            .body("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        $(soap()
            .server(imageServer)
            .send()
            .message()
            .body("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>cid:IMAGE</image:image>" +
                    "</image:getImageResponse>")
            .attachment(attachment)
            .mtomEnabled(true));

        $(soap()
            .client(imageClient)
            .receive()
            .message()
            .body("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>citrus:readFile(image/logo.base64)</image:image>" +
                    "</image:getImageResponse>"));
    }

}
