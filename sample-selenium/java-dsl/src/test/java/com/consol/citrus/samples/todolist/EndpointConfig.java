/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.remote.BrowserType;
import org.springframework.context.annotation.*;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public SeleniumBrowser browser() {
        return CitrusEndpoints.selenium()
                .browser()
                .type(BrowserType.CHROME)
                .build();
    }

    @Bean
    @DependsOn("browser")
    public SequenceAfterSuite afterSuite(SeleniumBrowser browser) {
        return new TestRunnerAfterSuiteSupport() {
            @Override
            public void afterSuite(TestRunner runner) {
                runner.selenium(builder -> builder.browser(browser).stop());
            }
        };
    }

    @Bean
    public SequenceAfterTest afterTest() {
        return new TestRunnerAfterTestSupport() {
            @Override
            public void afterTest(TestRunner runner) {
                runner.sleep(500);
            }
        };
    }

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints.http()
                            .client()
                            .requestUrl("http://localhost:8080")
                            .build();
    }
}
