package org.citrusframework.samples.quarkus.camel.rest.to.soap;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.FruitService;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.phase.Phase;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class FruitClientRoutes extends RouteBuilder {

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

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
    CxfEndpoint fruitSoapClient() {
        CxfEndpoint fruitEndpoint = new CxfEndpoint();
        fruitEndpoint.setServiceClass(FruitService.class);
        fruitEndpoint.setAddress(fruitServiceUrl);
        fruitEndpoint.getFeatures().add(prettyLoggingFeature);

        return fruitEndpoint;
    }

    @Override
    public void configure() throws Exception {
        onException(SoapFault.class)
                .handled(true)
                .process(this::processSoapFault);

        restConfiguration()
                .bindingMode(RestBindingMode.json);

        rest("/fruits").post()
                .type(Fruit.class)
                .produces(APPLICATION_JSON_CONTENT_TYPE)
                .to("direct:addFruit");
        rest("/fruits").get()
                .type(Fruit.class)
                .produces(APPLICATION_JSON_CONTENT_TYPE)
                .to("direct:listFruits");
        rest("/fruits").delete()
                .type(Fruit.class)
                .produces(APPLICATION_JSON_CONTENT_TYPE)
                .to("direct:deleteFruit");

        from("direct:listFruits")
                .setHeader(CxfConstants.OPERATION_NAME, constant("listFruits"))
                .to("cxf:bean:fruitSoapClient");

        from("direct:addFruit")
                .setHeader(CxfConstants.OPERATION_NAME, constant("addFruit"))
                .to("cxf:bean:fruitSoapClient");

        from("direct:deleteFruit")
                .setHeader(CxfConstants.OPERATION_NAME, constant("deleteFruit"))
                .to("cxf:bean:fruitSoapClient");
    }

    /**
     * Translate caught SOAP fault into an Http 500 response with the fault string as body content.
     */
    private void processSoapFault(Exchange exchange) {
        SoapFault caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SoapFault.class);
        exchange.getIn().setBody(caused.getMessage());
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
    }
}
