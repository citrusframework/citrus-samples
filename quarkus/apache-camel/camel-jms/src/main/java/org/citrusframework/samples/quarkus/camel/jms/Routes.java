package org.citrusframework.samples.quarkus.camel.jms;

import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jms:words-in")
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to("jms:words-out");
    }
}
