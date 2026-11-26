package org.citrusframework.samples.quarkus.camel.postgresql;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("platform-http:/headline?httpMethodRestrict=POST")
            .to("kamelet:postgresql-sink?" +
                    "serverName={{jdbc.server.host}}&" +
                    "serverPort={{jdbc.server.port}}&" +
                    "username={{jdbc.username}}&" +
                    "password={{jdbc.password}}&" +
                    "databaseName={{jdbc.database.name}}&" +
                    "query=INSERT INTO headlines VALUES (:#id,:#headline)")
            .setBody().constant("Headline created!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
    }
}
