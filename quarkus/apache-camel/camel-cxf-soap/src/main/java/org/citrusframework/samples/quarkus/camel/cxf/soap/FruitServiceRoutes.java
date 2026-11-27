package org.citrusframework.samples.quarkus.camel.cxf.soap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.test.fruitservice.FruitService;
import org.apache.cxf.ext.logging.LoggingFeature;

public class FruitServiceRoutes extends RouteBuilder {

    @Produces
    @ApplicationScoped
    @Named("prettyLoggingFeature")
    LoggingFeature prettyLoggingFeature() {
        final LoggingFeature result = new LoggingFeature();
        result.setPrettyLogging(true);
        return result;
    }

    @Inject
    @Named("prettyLoggingFeature")
    LoggingFeature prettyLoggingFeature;

    @Produces
    @ApplicationScoped
    @Named
    CxfEndpoint fruitEndpoint() {
        CxfEndpoint fruitEndpoint = new CxfEndpoint();
        fruitEndpoint.setServiceClass(FruitService.class);
        fruitEndpoint.getFeatures().add(prettyLoggingFeature);
        fruitEndpoint.setAddress("/fruits");

        return fruitEndpoint;
    }

    @Override
    public void configure() throws Exception {
        from("cxf:bean:fruitEndpoint")
                .recipientList(simple("bean:fruitService?method=${header.CamelCxfOperationName}"));
    }
}
