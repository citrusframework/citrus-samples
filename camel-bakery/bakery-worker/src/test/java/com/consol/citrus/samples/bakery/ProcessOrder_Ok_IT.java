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

package com.consol.citrus.samples.bakery;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
@Test
public class ProcessOrder_Ok_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("factoryOrderEndpoint")
    private JmsEndpoint factoryOrderEndpoint;

    @Autowired
    @Qualifier("reportingServer")
    private HttpServer reportingServer;

    @CitrusTest
    public void processOrderWithReporting() {
        send(factoryOrderEndpoint)
            .payload("<order type=\"cake\"><amount>1</amount></order>");

        receive(reportingServer)
            .http()
                .uri("/report/services/reporting")
                .method(HttpMethod.GET)
                .header("name", "cake")
                .header("amount", "1")
                .timeout(5000L);

        send(reportingServer)
            .http()
            .header(HttpMessageHeaders.HTTP_STATUS_CODE,
                    Integer.valueOf(HttpStatus.OK.value()));
    }
}
