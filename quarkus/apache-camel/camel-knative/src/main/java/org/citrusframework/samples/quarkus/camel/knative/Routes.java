package org.citrusframework.samples.quarkus.camel.knative;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataType;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:aws-s3-source")
                .transformDataType(new DataType("http:application-cloudevents"))
                .to("knative:event/org.apache.camel.event.messages?kind=Broker&name=default");
    }
}
