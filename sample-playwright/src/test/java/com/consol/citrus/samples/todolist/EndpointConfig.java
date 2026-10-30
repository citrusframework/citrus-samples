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

import org.citrusframework.api.container.AfterSuite;
import org.citrusframework.container.SequenceAfterSuite;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.playwright.actions.PlaywrightActionBuilder;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserType;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration providing the shared Playwright browser and the todo-app HTTP client.
 * The browser is stopped once by an after-suite hook after the whole test suite.
 */
@Import(TodoAppAutoConfiguration.class)
@Configuration
public class EndpointConfig {

    @Bean
    public PlaywrightBrowser browser() {
        return PlaywrightEndpoints
            .playwright()
                .browser()
                .browserType(PlaywrightBrowserType.CHROMIUM)
                .headless(true)
                .defaultTimeout(5000L)
            .build();
    }

    @Bean
    @DependsOn("browser")
    public AfterSuite afterSuite(PlaywrightBrowser browser) {
        return new SequenceAfterSuite.Builder()
                .actions(PlaywrightActionBuilder.playwright().browser(browser).stop())
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
