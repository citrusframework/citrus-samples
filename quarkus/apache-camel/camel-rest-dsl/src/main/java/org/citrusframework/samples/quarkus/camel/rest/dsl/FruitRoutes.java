package org.citrusframework.samples.quarkus.camel.rest.dsl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class FruitRoutes extends RouteBuilder {

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    private final List<Fruit> fruits = new LinkedList<>(List.of(
            new Fruit("Apple", "An apple is the round, edible fruit of an apple tree (Malus spp.)."),
            new Fruit("Mango", "A mango is an edible stone fruit produced by the tropical tree Mangifera indica."),
            new Fruit("Orange", "The orange, also called sweet orange to distinguish it from the bitter orange " +
                    "(Citrus x aurantium), is the fruit of a tree in the family Rutaceae.")
    ));

    @Override
    public void configure() throws Exception {
        rest("/fruits")
                .get()
                    .produces(APPLICATION_JSON_CONTENT_TYPE)
                    .to("direct:get-fruits")
                .post()
                    .consumes(APPLICATION_JSON_CONTENT_TYPE)
                    .to("direct:create-fruit")
                .delete()
                    .consumes(APPLICATION_JSON_CONTENT_TYPE)
                    .to("direct:delete-fruit");

        from("direct:get-fruits")
                .setBody(exchange -> new ArrayList<>(fruits))
                .marshal().json();

        from("direct:create-fruit")
                .unmarshal().json(Fruit.class)
                .process(exchange -> {
                    Fruit fruit = exchange.getIn().getBody(Fruit.class);
                    fruits.add(fruit);
                    exchange.getIn().setBody(null);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
                });

        from("direct:delete-fruit")
                .unmarshal().json(Fruit.class)
                .process(exchange -> {
                    Fruit fruit = exchange.getIn().getBody(Fruit.class);
                    boolean found = fruits.removeIf(f -> f.getName().equals(fruit.getName()));
                    exchange.getIn().setBody(null);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, !found ? 404 : 204);
                });
    }
}
