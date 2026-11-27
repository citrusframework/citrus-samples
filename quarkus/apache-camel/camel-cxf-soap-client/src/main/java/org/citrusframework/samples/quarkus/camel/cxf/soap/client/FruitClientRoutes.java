package org.citrusframework.samples.quarkus.camel.cxf.soap.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.test.fruitservice.FruitService;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class FruitClientRoutes extends RouteBuilder {

    @ConfigProperty(name = "fruit.service.url")
    String fruitServiceUrl;

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
        fruitEndpoint.setAddress(fruitServiceUrl);

        return fruitEndpoint;
    }

    @Override
    public void configure() throws Exception {
        from("direct:listFruits")
                .setHeader(CxfConstants.OPERATION_NAME, constant("listFruits"))
                .to("cxf:bean:fruitEndpoint");

        from("direct:addFruit")
                .setHeader(CxfConstants.OPERATION_NAME, constant("addFruit"))
                .to("cxf:bean:fruitEndpoint");

        from("direct:deleteFruit")
                .setHeader(CxfConstants.OPERATION_NAME, constant("deleteFruit"))
                .to("cxf:bean:fruitEndpoint");
    }
}
