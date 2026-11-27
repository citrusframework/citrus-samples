package org.citrusframework.samples.quarkus.camel.openapi.client;

import org.citrusframework.dsl.DefaultTestActions;
import org.citrusframework.TestActions;
import org.citrusframework.annotations.CitrusConfiguration;
import org.citrusframework.api.container.AfterSuite;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.spi.BindToRegistry;

import static org.citrusframework.container.SequenceAfterSuite.Builder.afterSuite;

@CitrusConfiguration
public class CitrusEndpointConfig {

    private final TestActions actions = new DefaultTestActions();

    HttpServer petstoreServer;

    @BindToRegistry
    public HttpServer petstoreServer() {
        if (petstoreServer == null) {
            petstoreServer = new HttpServerBuilder()
                .port(18080)
                .autoStart(true)
                .timeout(10000)
                .build();
        }

        return petstoreServer;
    }

    @BindToRegistry
    public AfterSuite afterSuiteActions() {
        return afterSuite()
                .actions(
                    actions.stop(petstoreServer()))
                .build();
    }
}
