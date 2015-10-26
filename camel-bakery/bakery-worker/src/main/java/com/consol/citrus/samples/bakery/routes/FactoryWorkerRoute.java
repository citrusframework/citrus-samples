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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Component
public class FactoryWorkerRoute extends RouteBuilder {

    @Value("${FACTORY_TYPE:default}")
    private String factoryType;

    @Value("${FACTORY_COSTS:1000}")
    private String factoryCosts;

    @Value("${REPORT_PORT_8080_TCP_ADDR:localhost}")
    private String reportServerHost;

    @Value("${REPORT_PORT_8080_TCP_PORT:18002}")
    private String reportServerPort;

    @Override
    public void configure() throws Exception {
        from("jms:queue:factory." + factoryType + ".inbound").routeId(factoryType + "_factory")
            .setHeader("name", xpath("order/@type"))
            .setHeader("id", xpath("order/id/text()"))
            .setHeader("amount", xpath("order/amount/text()"))
            .delay(constant(factoryCosts))
            .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
            .setBody(constant(""))
            .to("http://" + reportServerHost + ":" + reportServerPort + "/report/services/reporting");
    }
}
