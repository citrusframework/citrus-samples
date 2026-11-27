package org.citrusframework.samples.quarkus.camel.openapi.client;

import org.apache.camel.builder.RouteBuilder;

public class PetstoreClientRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:getPetById")
                .to("rest-openapi:petstore-api.json#getPetById?host={{petstore.service.url}}");

        from("direct:addPet")
                .marshal().json()
                .to("rest-openapi:petstore-api.json#addPet?host={{petstore.service.url}}");

        from("direct:updatePet")
                .marshal().json()
                .to("rest-openapi:petstore-api.json#updatePet?host={{petstore.service.url}}");

        from("direct:deletePet")
                .to("rest-openapi:petstore-api.json#deletePet?host={{petstore.service.url}}");
    }
}
