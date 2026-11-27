package org.citrusframework.samples.quarkus.camel.cxf.soap.client;

import java.util.Arrays;

import jakarta.xml.bind.JAXBElement;
import org.apache.camel.test.fruitservice.AddFruitResponse;
import org.apache.camel.test.fruitservice.DeleteFruitResponse;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.ListFruitsResponse;
import org.apache.camel.test.fruitservice.ObjectFactory;

public class FruitPojoHelper {

    static Fruit fruit(String name, String description) {
        Fruit fruit = new Fruit();
        fruit.setName(name);
        fruit.setDescription(description);
        return fruit;
    }

    static JAXBElement<AddFruitResponse> addFruitResponse(Fruit... fruits) {
        AddFruitResponse response = new AddFruitResponse();
        AddFruitResponse.Fruits responseFruits = new AddFruitResponse.Fruits();

        Arrays.stream(fruits).forEach(responseFruits.getFruit()::add);

        response.setFruits(responseFruits);
        return new ObjectFactory().createAddFruitResponse(response);
    }

    static JAXBElement<ListFruitsResponse> listFruitResponse(Fruit... fruits) {
        ListFruitsResponse response = new ListFruitsResponse();
        ListFruitsResponse.Fruits responseFruits = new ListFruitsResponse.Fruits();

        Arrays.stream(fruits).forEach(responseFruits.getFruit()::add);

        response.setFruits(responseFruits);
        return new ObjectFactory().createListFruitsResponse(response);
    }

    static JAXBElement<DeleteFruitResponse> deleteFruitResponse(Fruit... fruits) {
        DeleteFruitResponse response = new DeleteFruitResponse();
        DeleteFruitResponse.Fruits responseFruits = new DeleteFruitResponse.Fruits();

        Arrays.stream(fruits).forEach(responseFruits.getFruit()::add);

        response.setFruits(responseFruits);
        return new ObjectFactory().createDeleteFruitResponse(response);
    }
}
