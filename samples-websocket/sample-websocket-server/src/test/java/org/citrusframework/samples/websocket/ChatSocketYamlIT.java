package org.citrusframework.samples.websocket;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
@CitrusConfiguration(classes = {ChatSocketTest.EndpointConfig.class})
class ChatSocketYamlIT {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {"classpath:org/citrusframework/samples/websocket/should-broadcast-messages.citrus.it.yaml"})
    void shouldBroadcastMessages() {
    }
}
