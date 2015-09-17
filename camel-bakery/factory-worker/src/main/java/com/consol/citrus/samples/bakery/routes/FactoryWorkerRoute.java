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
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
@Component
public class FactoryWorkerRoute extends RouteBuilder implements Processor {

    @Override
    public void configure() throws Exception {
        from("jms:queue:factory.{{factory.type}}.inbound").routeId("{{factory.type}}_factory")
            .setHeader("name", xpath("order/@type"))
            .setHeader("amount", xpath("order/amount"))
            .process(this)
            .delay(simple("{{factory.costs}}"))
                .to("http://{{reporting.host}}:{{reporting.port}}/services/report");
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        in.setHeader(Exchange.HTTP_QUERY, String.format("name=%s&amount=%s", in.getHeader("name"), in.getHeader("amount")));
        in.setBody("");
    }
}
