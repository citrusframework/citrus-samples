package org.citrusframework.samples.quarkus.camel.knative.ssl;

import io.vertx.ext.web.client.WebClientOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.camel.CamelContext;
import org.apache.camel.component.knative.http.KnativeSslClientOptions;

@ApplicationScoped
public class SourceOptions {

    @Named("knativeHttpClientOptions")
    public WebClientOptions knativeHttpClientOptions(CamelContext camelContext) {
        return new KnativeSslClientOptions(camelContext);
    }
}
