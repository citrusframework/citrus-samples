package org.citrusframework.samples.quarkus.camel.mqtt;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:mqtt5-source?brokerUrl={{mqtt.broker.url}}&topic={{mqtt.topic}}")
            .transform().jq(".value")
            .convertBodyTo(Integer.class)
            .choice()
                .when().simple("${body} > 20")
                    .log("Warm temperature: ${body}")
                    .to(kafka("temperature-warm"))
                .otherwise()
                    .log("Cold temperature: ${body}")
                    .to(kafka("temperature-cold"))
            .end();
    }
}
