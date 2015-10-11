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
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Test
public class LoadTest_Ok_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("bakeryOrderEndpoint")
    private JmsEndpoint bakeryOrderEndpoint;

    @CitrusTest
    public void loadCakeOrder() {
        send(bakeryOrderEndpoint)
                .payload("<order type=\"cake\"><id>citrus:randomNumber(10)</id><amount>1</amount></order>");
    }

    @CitrusTest
    public void loadPretzelOrder() {
        send(bakeryOrderEndpoint)
                .payload("<order type=\"pretzel\"><id>citrus:randomNumber(10)</id><amount>1</amount></order>");
    }

    @CitrusTest
    public void loadBreadOrder() {
        send(bakeryOrderEndpoint)
                .payload("<order type=\"bread\"><id>citrus:randomNumber(10)</id><amount>1</amount></order>");
    }
}
