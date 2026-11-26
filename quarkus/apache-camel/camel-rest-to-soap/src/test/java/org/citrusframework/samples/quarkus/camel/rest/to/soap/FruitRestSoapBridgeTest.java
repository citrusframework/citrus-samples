package org.citrusframework.samples.quarkus.camel.rest.to.soap;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.ws.config.annotation.WebServiceServerConfig;
import org.citrusframework.ws.server.WebServiceServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@CitrusSupport
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitRestSoapBridgeTest implements TestActionSupport {

    private static final String TARGET_NS = "http://camel.apache.org/test/FruitService";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18080, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldListFruits() {
        runner.given(createVariables()
                .variable("targetNS", TARGET_NS));

        runner.when(
                http().client(fruitRestClient)
                    .send()
                    .get("/fruits")
                    .fork(true)
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

        runner.when(
                http().client(fruitRestClient)
                        .send()
                        .post("/fruits")
                        .message()
                        .body("""
                        {
                          "name": "Mango",
                          "description": "Tropical fruit"
                        }
                        """)
                        .contentType(APPLICATION_JSON_CONTENT_TYPE)
                        .fork(true)
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

        runner.when(
                http().client(fruitRestClient)
                    .send()
                    .delete("/fruits")
                    .message()
                    .body("""
                    {
                      "name": "Apple",
                      "description": "Winter fruit"
                    }
                    """)
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .fork(true)
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

        runner.when(
                http().client(fruitRestClient)
                    .send()
                    .delete("/fruits")
                    .message()
                    .body("""
                    {
                      "name": "Pineapple",
                      "description": "Tropical fruit"
                    }
                    """)
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .fork(true)
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
                http().client(fruitRestClient)
                        .receive()
                        .response(500)
                        .message()
                        .validate((message, context) -> {
                            Assertions.assertEquals("Fruit \"Pineapple\" does not exist.", message.getPayload());
                        })
        );
    }
}
