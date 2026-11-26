package org.citrusframework.samples.quarkus.camel.cxf.soap;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.config.annotation.WebServiceClientConfig;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@QuarkusTest
@CitrusSupport
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FruitSoapServiceTest implements TestActionSupport {

    private static final String TARGET_NS = "http://camel.apache.org/test/FruitService";

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8081/cxf/services/fruits")
    WebServiceClient soapClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    public SoapMessageFactory messageFactory() {
        SaajSoapMessageFactory factory = new SaajSoapMessageFactory();
        factory.afterPropertiesSet();
        return factory;
    }

    @Test
    @Order(1)
    void shouldListFruits() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:listFruits xmlns:ns=\"${targetNS}\"/>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:listFruitsResponse xmlns:ns=\"${targetNS}\">" +
                                "<fruits>" +
                                    "<fruit><description>Winter fruit</description><name>Apple</name></fruit>" +
                                    "<fruit><description>Citrus fruit</description><name>Orange</name></fruit>" +
                                "</fruits>" +
                            "</ns:listFruitsResponse>")
        );
    }

    @Test
    @Order(2)
    void shouldAddFruit() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:addFruit xmlns:ns=\"${targetNS}\">" +
                                "<fruit><description>Tropical fruit</description><name>Mango</name></fruit>" +
                            "</ns:addFruit>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:addFruitResponse xmlns:ns=\"${targetNS}\">" +
                                "<fruits>" +
                                    "<fruit><description>Winter fruit</description><name>Apple</name></fruit>" +
                                    "<fruit><description>Citrus fruit</description><name>Orange</name></fruit>" +
                                    "<fruit><description>Tropical fruit</description><name>Mango</name></fruit>" +
                                "</fruits>" +
                            "</ns:addFruitResponse>")
        );
    }

    @Test
    @Order(3)
    void shouldDeleteFruit() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:deleteFruit xmlns:ns=\"${targetNS}\">" +
                                "<fruit><description>Winter fruit</description><name>Apple</name></fruit>" +
                            "</ns:deleteFruit>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:deleteFruitResponse xmlns:ns=\"${targetNS}\">" +
                                "<fruits>" +
                                    "<fruit><description>Citrus fruit</description><name>Orange</name></fruit>" +
                                    "<fruit><description>Tropical fruit</description><name>Mango</name></fruit>" +
                                "</fruits>" +
                            "</ns:deleteFruitResponse>")
        );
    }

    @Test
    @Order(4)
    void shouldHandleFruitNotFound() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.then(
                soap().client(soapClient)
                        .assertFault()
                        .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Server")
                        .faultString("Fruit \"Pineapple\" does not exist.")
                        .when(soap().client(soapClient)
                                .send()
                                .message()
                                .soapAction("")
                                .body("<ns:deleteFruit xmlns:ns=\"${targetNS}\">" +
                                        "<fruit><description>Tropical fruit</description><name>Pineapple</name></fruit>" +
                                    "</ns:deleteFruit>"))
        );
    }
}
