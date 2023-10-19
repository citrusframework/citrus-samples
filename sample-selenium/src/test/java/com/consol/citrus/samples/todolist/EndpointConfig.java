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

import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.remote.Browser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.selenium.actions.SeleniumActionBuilder.selenium;

/**
 * @author Christoph Deppisch
 */
@Import(TodoAppAutoConfiguration.class)
@Configuration
public class EndpointConfig {

    @Bean
    public SeleniumBrowser browser() {
        return CitrusEndpoints
            .selenium()
                .browser()
                .type(Browser.HTMLUNIT.browserName())
            .build();
    }

    @Bean
    @DependsOn("browser")
    public AfterSuite afterSuite(SeleniumBrowser browser) {
        return new SequenceAfterSuite.Builder()
                .actions(selenium().browser(browser).stop())
                .build();
    }

    @Bean
    public AfterTest afterTest() {
        return new SequenceAfterTest.Builder()
                .actions(sleep().milliseconds(500L))
                .build();
    }

    @Bean
    public HttpClient todoClient() {
        return CitrusEndpoints
            .http()
                .client()
                .requestUrl("http://localhost:8080")
            .build();
    }
}
