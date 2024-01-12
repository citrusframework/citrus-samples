package org.apache.camel.demo;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.citrusframework.container.AfterSuite;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;
import org.citrusframework.spi.BindToRegistry;
import org.openqa.selenium.remote.Browser;

import static org.citrusframework.actions.StopServerAction.Builder.stop;
import static org.citrusframework.container.SequenceAfterSuite.Builder.afterSuite;
import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;
import static org.citrusframework.kafka.endpoint.builder.KafkaEndpoints.kafka;
import static org.citrusframework.mail.endpoint.builder.MailEndpoints.mail;

public class CitrusEndpointConfig {

    private MailServer mailServer;
    private HttpServer shippingService;

    @BindToRegistry
    public HttpClient foodMarketApiClient() {
        return http().client()
                .requestUrl("http://localhost:8081")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint products() {
        return kafka()
                .asynchronous()
                .topic("products")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint bookings() {
        return kafka()
                .asynchronous()
                .topic("bookings")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint supplies() {
        return kafka()
                .asynchronous()
                .topic("supplies")
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint shipping() {
        return kafka()
                .asynchronous()
                .topic("shipping")
                .consumerGroup("citrus-shipping")
                .timeout(10000L)
                .build();
    }

    @BindToRegistry
    public KafkaEndpoint completed() {
        return kafka()
                .asynchronous()
                .topic("completed")
                .consumerGroup("citrus-completed")
                .timeout(10000L)
                .build();
    }

    @BindToRegistry
    public HttpServer shippingService() {
        if (shippingService == null) {
            shippingService = new HttpServerBuilder()
                    .port(8888)
                    .timeout(10000L)
                    .autoStart(true)
                    .build();
        }

        return shippingService;
    }

    @BindToRegistry
    public MailServer mailServer() {
        if (mailServer == null) {
            mailServer = mail().server()
                    .port(2222)
                    .knownUsers(Collections.singletonList("foodmarket@quarkus.io:foodmarket:secr3t"))
                    .autoAccept(true)
                    .autoStart(true)
                    .build();
        }

        return mailServer;
    }

    @BindToRegistry
    public ObjectMapper mapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
                .build()
                .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
    }

    @BindToRegistry
    public SeleniumBrowser browser() {
        return new SeleniumBrowserBuilder()
                .type(Browser.HTMLUNIT.browserName())
                .build();
    }

    @BindToRegistry
    public AfterSuite afterSuiteActions() {
        return afterSuite()
                .actions(
                    stop(mailServer(), shippingService()))
                .build();
    }

}
