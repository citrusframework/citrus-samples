/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.bakery.routes;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Component
public class BakeryRouter extends RouteBuilder implements Processor {

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .contextPath("bakery/services")
                .port(18001)
                .dataFormatProperty("prettyPrint", "true");

        rest("/order")
            .consumes("application/json")
            .post().route().unmarshal().json(JsonLibrary.Jackson).process(this).to("direct:bakery");

        from("jms:queue:bakery.order.inbound").routeId("bakery_jms_inbound")
            .to("direct:bakery");

        from("direct:bakery").routeId("bakery")
            .choice()
                .when(xpath("order/@type = 'bread'"))
                    .inOnly("jms:queue:factory.bread.inbound")
                .when(xpath("order/@type = 'pretzel'"))
                    .inOnly("jms:queue:factory.pretzel.inbound")
                .when(xpath("order/@type = 'cake'"))
                    .inOnly("jms:queue:factory.cake.inbound")
                .otherwise()
                    .inOnly("jms:queue:factory.unknown.inbound")
            .end()
            .setBody(simple(""));
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        Map<String, Object> orderJson = (Map<String, Object>) in.getBody(Map.class).get("order");
        in.setBody("<order type=\"" + orderJson.get("type") + "\">" +
                "<id>" + orderJson.get("id") + "</id>" +
                "<amount>" + orderJson.get("amount") + "</amount>" +
            "</order>");

        in.getHeaders().remove(Exchange.HTTP_BASE_URI);
        in.getHeaders().remove(Exchange.HTTP_METHOD);
        in.getHeaders().remove(Exchange.HTTP_PATH);
        in.getHeaders().remove(Exchange.HTTP_URI);
        in.getHeaders().remove(Exchange.HTTP_URL);
    }
}
