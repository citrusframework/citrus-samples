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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class PlaceUnknownOrderIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    @Qualifier("bakeryOrderEndpoint")
    private JmsEndpoint bakeryOrderEndpoint;

    @Autowired
    @Qualifier("unknownOrderEndpoint")
    private JmsEndpoint unknownOrderEndpoint;

    @CitrusTest
    public void placeUnknownOrderType() {
        $(send()
            .endpoint(bakeryOrderEndpoint)
            .message()
            .body("<order><type>brownie</type><id>999</id><amount>1</amount></order>"));

        $(receive()
            .endpoint(unknownOrderEndpoint)
            .message()
            .body("<order><type>brownie</type><id>999</id><amount>1</amount></order>"));
    }
}
