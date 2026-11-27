package org.citrusframework.samples.quarkus.camel.cxf.soap.client;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.fruitservice.Fruit;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.message.CamelMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.ws.config.annotation.WebServiceServerConfig;
import org.citrusframework.ws.server.WebServiceServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@CitrusSupport
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitSoapClientTest implements TestActionSupport {

    private static final String TARGET_NS = "http://camel.apache.org/test/FruitService";

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18080, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldListFruits() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.when(
                async().actions(
                        action(context -> {
                            ProducerTemplate template = camelContext.createProducerTemplate();
                            template.requestBody("direct:listFruits", (Object) null);
                        })
                )
        );

        runner.then(
                soap().server(soapServer)
                        .receive()
                        .message()
                        .body("""
                            <ns:listFruits xmlns:ns="${targetNS}"/>
                            """)
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body("""
                            <ns:listFruitsResponse xmlns:ns="${targetNS}">
                                <fruits>
                                    <fruit><description>Winter fruit</description><name>Apple</name></fruit>
                                    <fruit><description>Citrus fruit</description><name>Orange</name></fruit>
                                </fruits>
                            </ns:listFruitsResponse>
                            """)
        );
    }

    @Test
    void shouldAddFruit() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        Fruit mango = new Fruit();
        mango.setName("Mango");
        mango.setDescription("Tropical fruit");

        runner.when(
                camel().send()
                        .endpoint("direct:addFruit")
                        .fork(true)
                        .message(new DefaultMessage(mango))
        );

        runner.then(
                soap().server(soapServer)
                        .receive()
                        .message()
                        .body("""
                        <ns:addFruit xmlns:ns="${targetNS}">
                            <fruit><description>Tropical fruit</description><name>Mango</name></fruit>
                        </ns:addFruit>
                        """)
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body("""
                            <ns:addFruitResponse xmlns:ns="${targetNS}">
                                <fruits>
                                    <fruit><description>Winter fruit</description><name>Apple</name></fruit>
                                    <fruit><description>Citrus fruit</description><name>Orange</name></fruit>
                                    <fruit><description>Tropical fruit</description><name>Mango</name></fruit>
                                </fruits>
                            </ns:addFruitResponse>
                            """)
        );
    }

    @Test
    void shouldDeleteFruit() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        Fruit apple = new Fruit();
        apple.setName("Apple");
        apple.setDescription("Winter fruit");

        runner.when(
                camel().send()
                        .endpoint("direct:deleteFruit")
                        .fork(true)
                        .message(new DefaultMessage(apple))
        );

        runner.then(
                soap().server(soapServer)
                        .receive()
                        .message()
                        .body("""
                            <ns:deleteFruit xmlns:ns="${targetNS}">
                                <fruit><description>Winter fruit</description><name>Apple</name></fruit>
                            </ns:deleteFruit>
                            """)
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body("""
                            <ns:deleteFruitResponse xmlns:ns="${targetNS}">
                                <fruits>
                                    <fruit><description>Citrus fruit</description><name>Orange</name></fruit>
                                </fruits>
                            </ns:deleteFruitResponse>
                            """)
        );
    }

    @Test
    void shouldHandleSoapFault() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        Fruit pineapple = new Fruit();
        pineapple.setName("Pineapple");
        pineapple.setDescription("Tropical fruit");

        runner.when(
                camel().send()
                        .endpoint("direct:deleteFruit", true)
                        .fork(true)
                        .message(new DefaultMessage(pineapple))
        );

        runner.then(
                soap().server(soapServer)
                        .receive()
                        .message()
                        .body("""
                            <ns:deleteFruit xmlns:ns="${targetNS}">
                                <fruit><description>Tropical fruit</description><name>Pineapple</name></fruit>
                            </ns:deleteFruit>
                            """)
        );

        runner.then(
                soap().server(soapServer)
                        .sendFault()
                        .message()
                        .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Server")
                        .faultString("Fruit \"Pineapple\" does not exist.")
        );

        runner.then(
                camel().receive()
                        .endpoint("direct:deleteFruit", true)
                        .message()
                        .validate((message, context) -> {
                            Assertions.assertNotNull(message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION));
                            Assertions.assertEquals("Fruit \"Pineapple\" does not exist.", message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE));
                        })
        );
    }
}
