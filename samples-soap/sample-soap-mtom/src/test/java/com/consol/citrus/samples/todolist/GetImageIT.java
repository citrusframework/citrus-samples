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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.ws.validation.BinarySoapAttachmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GetImageIT extends TestNGCitrusTestRunner {

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

        soap(soapActionBuilder -> soapActionBuilder
            .client(imageClient)
            .send()
            .fork(true)
            .soapAction("getImage")
            .payload("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        soap(soapActionBuilder -> soapActionBuilder
            .server(imageServer)
            .receive()
            .soapAction("getImage")
            .payload("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        soap(soapActionBuilder -> soapActionBuilder
            .server(imageServer)
            .send()
            .payload("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>cid:IMAGE</image:image>" +
                    "</image:getImageResponse>")
            .attachment(attachment)
            .mtomEnabled(true));

        soap(soapActionBuilder -> soapActionBuilder
            .client(imageClient)
            .receive()
            .schemaValidation(false)
            .payload("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
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

        soap(soapActionBuilder -> soapActionBuilder
            .client(imageClient)
            .send()
            .fork(true)
            .soapAction("getImage")
            .payload("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        soap(soapActionBuilder -> soapActionBuilder
            .server(imageServer)
            .receive()
            .soapAction("getImage")
            .payload("<image:getImage xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:id>IMAGE</image:id>" +
                    "</image:getImage>"));

        soap(soapActionBuilder -> soapActionBuilder
            .server(imageServer)
            .send()
            .payload("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>cid:IMAGE</image:image>" +
                    "</image:getImageResponse>")
            .attachment(attachment)
            .mtomEnabled(true));

        soap(soapActionBuilder -> soapActionBuilder
            .client(imageClient)
            .receive()
            .payload("<image:getImageResponse xmlns:image=\"http://www.citrusframework.org/imageService\">" +
                        "<image:image>citrus:readFile(image/logo.base64)</image:image>" +
                    "</image:getImageResponse>"));
    }

}
