package org.citrusframework.samples.quarkus.camel.direct.http;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:words-in")
            .to("direct:translate")
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to("mock:words-out");

        from("direct:translate")
            .choice()
                .when(simple("${header.lang} != null"))
                    .setVariable("lang", simple("${header.lang}"))
                    .removeHeaders("*")
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .setHeader(Exchange.HTTP_QUERY, simple("lang=${variable.lang}"))
                    .to("http://{{camel.translate.service.host}}:{{camel.translate.service.port}}/translate")
                    .convertBodyTo(String.class)
                .otherwise()
                    .setBody(simple("${body}"));
    }
}
