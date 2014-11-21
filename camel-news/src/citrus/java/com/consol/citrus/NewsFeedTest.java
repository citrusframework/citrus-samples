package com.consol.citrus;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0.1
 */
@Test
public class NewsFeedTest extends TestNGCitrusTestBuilder {

    @CitrusTest(name = "NewsFeed_Ok_Test")
    public void newsFeed_Ok_Test() {
        send("newsJmsEndpoint")
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>");

        receive("newsSoapServer")
                .payload("<nf:News xmlns:nf=\"http://citrusframework.org/schemas/samples/news\">" +
                            "<nf:Message>Citrus rocks!</nf:Message>" +
                        "</nf:News>")
                .header(SoapMessageHeaders.SOAP_ACTION, "newsFeed");

        send("newsSoapServer")
                .header(SoapMessageHeaders.HTTP_STATUS_CODE, "200");
    }

}
