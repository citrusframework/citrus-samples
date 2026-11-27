package org.citrusframework.samples.quarkus.camel.aws.s3.kafka;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:aws-s3-source")
            .split(body().tokenize("\n"))
            .filter(simple("${body} != \"\""))
            .setBody()
                .simple("""
                    { "message": "${body}" }
                    """)
            .to(kafka("s3-events"));
    }
}
