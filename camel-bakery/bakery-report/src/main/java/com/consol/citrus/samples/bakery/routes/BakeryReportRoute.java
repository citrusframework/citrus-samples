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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
@Component
public class BakeryReportRoute extends RouteBuilder implements Processor {

    /** in memory report **/
    private Map<String, AtomicInteger> report = new HashMap<>();

    @Override
    public void configure() throws Exception {
        from("servlet:///report?servletName=bakery-report").routeId("bakery_report")
            .process(this);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        if (in.getHeader("name") != null) {
            String name = in.getHeader("name", String.class);
            Integer amount = in.getHeader("amount", Integer.class);
            if (report.containsKey(name)) {
                for (int i = 0; i < amount; i++) {
                    report.get(name).incrementAndGet();
                }
            } else {
                report.put(name, new AtomicInteger(amount));
            }
        }

        if (in.getHeader("type") != null && in.getHeader("type", String.class).equals("json")) {
            in.setBody(report.toString());
        } else {
            in.setBody(htmlReport());
        }
    }

    private String htmlReport() {
        StringBuilder response = new StringBuilder();
        response.append("<html><body><h1>Camel bakery reporting</h1><p>Today we have produced following goods:</p>");

        response.append("<ul>");

        for (Map.Entry<String, AtomicInteger> goods : report.entrySet()) {
            response.append("<li>")
                    .append(goods.getKey()).append(":").append(goods.getValue().get())
                    .append("</li>");
        }

        response.append("</ul>");
        response.append("</body></html>");

        return response.toString();
    }

}
