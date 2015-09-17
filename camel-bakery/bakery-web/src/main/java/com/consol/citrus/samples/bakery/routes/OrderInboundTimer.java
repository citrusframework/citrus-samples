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
public class OrderInboundTimer extends RouteBuilder implements Processor {

    private String[] orderTypes = new String[] {"bread", "pretzel", "cake"};

    @Override
    public void configure() throws Exception {
        from("timer://orders?fixedRate=true&period=5s")
            .routeId("order_timer")
            .autoStartup(false)
            .process(this)
            .to("jms:queue:bakery.order.inbound");
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        int index = exchange.getProperty(Exchange.TIMER_COUNTER, Integer.class);

        String orderType = orderTypes[index++ % orderTypes.length];

        in.setBody("<order type=\"" + orderType + "\">" +
                        "<id>" + index + "</id>" +
                        "<amount>1</amount>" +
                    "</order>");

        in.setHeader("orderType", orderType);
    }
}
