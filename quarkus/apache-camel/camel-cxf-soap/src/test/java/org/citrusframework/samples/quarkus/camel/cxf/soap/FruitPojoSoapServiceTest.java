package org.citrusframework.samples.quarkus.camel.cxf.soap;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import org.apache.camel.test.fruitservice.AddFruit;
import org.apache.camel.test.fruitservice.AddFruitResponse;
import org.apache.camel.test.fruitservice.DeleteFruit;
import org.apache.camel.test.fruitservice.DeleteFruitResponse;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.ListFruits;
import org.apache.camel.test.fruitservice.ListFruitsResponse;
import org.apache.camel.test.fruitservice.ObjectFactory;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.config.annotation.WebServiceClientConfig;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.citrusframework.xml.message.builder.MarshallingPayloadBuilder.Builder.marshal;

@QuarkusTest
@CitrusSupport
public class FruitPojoSoapServiceTest implements TestActionSupport {

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

    @BindToRegistry
    public Jaxb2Marshaller marshaller() {
        return new Jaxb2Marshaller(Fruit.class.getPackageName());
    }

    @Test
    void shouldListFruits() {
        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body(marshal(new ObjectFactory().createListFruits(new ListFruits())))
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<ListFruitsResponse>>() {
                            @Override
                            public void validate(JAXBElement<ListFruitsResponse> payload, Map<String, Object> headers, TestContext context) {
                                ListFruitsResponse response = payload.getValue();

                                Assertions.assertFalse(response.getFruits().getFruit().isEmpty());
                                Assertions.assertTrue(response.getFruits().getFruit().stream()
                                        .anyMatch(fruit -> "Apple".equals(fruit.getName())));
                                Assertions.assertTrue(response.getFruits().getFruit().stream()
                                        .anyMatch(fruit -> "Orange".equals(fruit.getName())));
                            }
                        })
        );
    }

    @Test
    void shouldAddFruit() {
        Fruit mango = new Fruit();
        mango.setName("Mango");
        mango.setDescription("Tropical fruit");

        AddFruit addFruit = new AddFruit();
        addFruit.setFruit(mango);

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body(marshal(new ObjectFactory().createAddFruit(addFruit)))
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<AddFruitResponse>>() {
                            @Override
                            public void validate(JAXBElement<AddFruitResponse> payload, Map<String, Object> headers, TestContext context) {
                                AddFruitResponse response = payload.getValue();

                                Assertions.assertFalse(response.getFruits().getFruit().isEmpty());
                                Assertions.assertTrue(response.getFruits().getFruit().stream()
                                        .anyMatch(fruit -> "Mango".equals(fruit.getName())));
                            }
                        })
        );
    }

    @Test
    void shouldDeleteFruit() {
        Fruit apple = new Fruit();
        apple.setName("Apple");
        apple.setDescription("Winter fruit");

        DeleteFruit deleteFruit = new DeleteFruit();
        deleteFruit.setFruit(apple);

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body(marshal(new ObjectFactory().createDeleteFruit(deleteFruit)))
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .validate(new XmlMarshallingValidationProcessor<JAXBElement<DeleteFruitResponse>>() {
                            @Override
                            public void validate(JAXBElement<DeleteFruitResponse> payload, Map<String, Object> headers, TestContext context) {
                                DeleteFruitResponse response = payload.getValue();
                                Assertions.assertTrue(response.getFruits().getFruit().stream()
                                        .noneMatch(fruit -> "Apple".equals(fruit.getName())));
                            }
                        })
        );
    }

    @Test
    void shouldHandleFruitNotFound() {
        Fruit pineapple = new Fruit();
        pineapple.setName("Pineapple");
        pineapple.setDescription("Tropical fruit");

        DeleteFruit deleteFruit = new DeleteFruit();
        deleteFruit.setFruit(pineapple);

        runner.then(
                soap().client(soapClient)
                        .assertFault()
                        .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Server")
                        .faultString("Fruit \"Pineapple\" does not exist.")
                        .when(soap().client(soapClient)
                                .send()
                                .message()
                                .soapAction("")
                                .body(marshal(new ObjectFactory().createDeleteFruit(deleteFruit))))
        );
    }
}
