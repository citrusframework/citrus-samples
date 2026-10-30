package com.consol.citrus.samples.gradle;

import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = { EndpointConfig.class })
public class MessagingTest extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private DirectEndpoint testEndpoint;

    @Test
    @CitrusTest
    public void testMessaging(@Optional @CitrusResource TestActionRunner actions) {
        actions.$(echo("Test simple message send and receive"));

        actions.$(send()
            .endpoint(testEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Hello Citrus!"));

        actions.$(receive()
            .endpoint(testEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Hello Citrus!"));

        actions.$(echo("Successful send and receive"));
    }
}
