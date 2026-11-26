package org.citrusframework.samples.quarkus.camel.cxf.soap.client;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBElement;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.fruitservice.AddFruit;
import org.apache.camel.test.fruitservice.DeleteFruit;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.ListFruits;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.ws.config.annotation.WebServiceServerConfig;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.citrusframework.samples.quarkus.camel.cxf.soap.client.FruitPojoHelper.addFruitResponse;
import static org.citrusframework.samples.quarkus.camel.cxf.soap.client.FruitPojoHelper.deleteFruitResponse;
import static org.citrusframework.samples.quarkus.camel.cxf.soap.client.FruitPojoHelper.fruit;
import static org.citrusframework.samples.quarkus.camel.cxf.soap.client.FruitPojoHelper.listFruitResponse;
import static org.citrusframework.xml.message.builder.MarshallingPayloadBuilder.Builder.marshal;

@QuarkusTest
@CitrusSupport
@TestProfile(FruitPojoSoapClientTest.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitPojoSoapClientTest implements TestActionSupport {

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18081, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    public Jaxb2Marshaller marshaller() {
        return new Jaxb2Marshaller(Fruit.class.getPackageName());
    }

    @Test
    void shouldListFruits() {
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
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<ListFruits>>() {
                            @Override
                            public void validate(JAXBElement<ListFruits> payload, Map<String, Object> headers, TestContext context) {
                                // listFruits has no parameters - just verify the request type
                            }
                        })
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body(marshal(listFruitResponse(
                                fruit("Apple", "Winter fruit"),
                                fruit("Orange", "Citrus fruit")
                        )))
        );
    }

    @Test
    void shouldAddFruit() {
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
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<AddFruit>>() {
                            @Override
                            public void validate(JAXBElement<AddFruit> payload, Map<String, Object> headers, TestContext context) {
                                AddFruit request = payload.getValue();
                                Assertions.assertEquals("Mango", request.getFruit().getName());
                                Assertions.assertEquals("Tropical fruit", request.getFruit().getDescription());
                            }
                        })
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body(marshal(addFruitResponse(
                                fruit("Apple", "Winter fruit"),
                                fruit("Orange", "Citrus fruit"),
                                mango
                        )))
        );
    }

    @Test
    void shouldDeleteFruit() {
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
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<DeleteFruit>>() {
                            @Override
                            public void validate(JAXBElement<DeleteFruit> payload, Map<String, Object> headers, TestContext context) {
                                DeleteFruit request = payload.getValue();
                                Assertions.assertEquals("Apple", request.getFruit().getName());
                            }
                        })
        );

        runner.then(
                soap().server(soapServer)
                        .send()
                        .message()
                        .body(marshal(deleteFruitResponse(fruit("Orange", "Citrus fruit"))))
        );
    }

    public static class TestConfig implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("fruit.service.url", "http://localhost:18081/services/fruits");
        }
    }
}
