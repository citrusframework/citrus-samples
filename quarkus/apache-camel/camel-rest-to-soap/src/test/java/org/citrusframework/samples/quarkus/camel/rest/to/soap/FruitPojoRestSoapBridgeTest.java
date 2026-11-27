package org.citrusframework.samples.quarkus.camel.rest.to.soap;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.xml.bind.JAXBElement;
import org.apache.camel.test.fruitservice.AddFruit;
import org.apache.camel.test.fruitservice.DeleteFruit;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.ListFruits;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.json.dsl.JsonSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.ws.config.annotation.WebServiceServerConfig;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.citrusframework.samples.quarkus.camel.rest.to.soap.FruitPojoHelper.addFruitResponse;
import static org.citrusframework.samples.quarkus.camel.rest.to.soap.FruitPojoHelper.deleteFruitResponse;
import static org.citrusframework.samples.quarkus.camel.rest.to.soap.FruitPojoHelper.fruit;
import static org.citrusframework.samples.quarkus.camel.rest.to.soap.FruitPojoHelper.listFruitResponse;
import static org.citrusframework.xml.message.builder.MarshallingPayloadBuilder.Builder.marshal;

@QuarkusTest
@CitrusSupport
@TestProfile(FruitPojoRestSoapBridgeTest.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FruitPojoRestSoapBridgeTest implements TestActionSupport {

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusEndpoint
    @WebServiceServerConfig(port = 18081, autoStart = true, timeout = 10000)
    WebServiceServer soapServer;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    @BindToRegistry
    public Jaxb2Marshaller marshaller() {
        return new Jaxb2Marshaller(Fruit.class.getPackageName());
    }

    @Test
    void shouldListFruits() {
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
                http().client(fruitRestClient)
                    .send()
                    .post("/fruits")
                    .message()
                    .body(JsonSupport.marshal(mango))
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .fork(true)
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
                http().client(fruitRestClient)
                    .send()
                    .delete("/fruits")
                    .message()
                    .body(JsonSupport.marshal(apple))
                    .contentType(APPLICATION_JSON_CONTENT_TYPE)
                    .fork(true)
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
