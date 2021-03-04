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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.ws.validation.BinarySoapAttachmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static com.consol.citrus.dsl.XmlSupport.xml;
import static com.consol.citrus.ws.actions.SoapActionBuilder.soap;

/**
 * @author Christoph Deppisch
 */
public class AddImageIT extends TestNGCitrusSpringSupport {

    @Autowired
    private WebServiceClient imageClient;

    @Autowired
    private WebServiceServer imageServer;

    @Test
    @CitrusTest
    public void testAddImageMtom() {
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
            .soapAction("addImage")
            .body("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                    "<image:id>logo</image:id>" +
                    "<image:image>cid:IMAGE</image:image>" +
                    "</image:addImage>")
            .attachment(attachment)
            .mtomEnabled(true));

        $(soap()
            .server(imageServer)
            .receive()
            .message()
            .soapAction("addImage")
            .validate(xml()
                        .schemaValidation(false))
            .body("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>logo</image:id>" +
                        "<image:image>" +
                            "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:IMAGE\"/>" +
                        "</image:image>" +
                    "</image:addImage>")
            .attachmentValidator(new BinarySoapAttachmentValidator())
            .attachment(attachment));

        $(soap()
            .server(imageServer)
            .send()
            .message()
            .body("<image:addImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:success>true</image:success>" +
                    "</image:addImageResponse>"));

        $(soap()
            .client(imageClient)
            .receive()
            .message()
            .body("<image:addImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:success>true</image:success>" +
                    "</image:addImageResponse>"));
    }

    @Test
    @CitrusTest
    public void testAddImageMtomInline() {
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
            .soapAction("addImage")
            .body("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>logo</image:id>" +
                        "<image:image>cid:IMAGE</image:image>" +
                    "</image:addImage>")
            .attachment(attachment)
            .mtomEnabled(true));

        $(soap()
            .server(imageServer)
            .receive()
            .message()
            .soapAction("addImage")
            .body("<image:addImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>logo</image:id>" +
                        "<image:image>citrus:readFile(image/logo.base64)</image:image>" +
                    "</image:addImage>"));

        $(soap()
            .server(imageServer)
            .send()
            .message()
            .body("<image:addImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:success>true</image:success>" +
                    "</image:addImageResponse>"));

        $(soap()
            .client(imageClient)
            .receive()
            .message()
            .body("<image:addImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:success>true</image:success>" +
                    "</image:addImageResponse>"));
    }
}
