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

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Component
public class BakeryReportRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration()
            .component("servlet")
            .contextPath("report/services")
            .port(18002)
            .dataFormatProperty("prettyPrint", "true");

        rest("/reporting")
            .put().to("bean:reportService?method=add(${header.id}, ${header.name}, ${header.amount})")
            .get().to("bean:reportService?method=html")
            .get("/order").to("bean:reportService?method=status(${header.id})")
            .get("/orders").to("bean:reportService?method=orders()")
            .get("/json").to("bean:reportService?method=json")
            .get("/reset").to("bean:reportService?method=reset");
    }
}
