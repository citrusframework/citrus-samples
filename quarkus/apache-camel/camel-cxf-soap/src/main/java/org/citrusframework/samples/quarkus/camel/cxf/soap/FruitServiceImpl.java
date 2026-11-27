package org.citrusframework.samples.quarkus.camel.cxf.soap;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.camel.test.fruitservice.AddFruitResponse;
import org.apache.camel.test.fruitservice.DeleteFruitResponse;
import org.apache.camel.test.fruitservice.Fruit;
import org.apache.camel.test.fruitservice.FruitService;
import org.apache.camel.test.fruitservice.ListFruitsResponse;
import org.apache.camel.test.fruitservice.NoSuchFruitException;

@ApplicationScoped
@Named("fruitService")
public class FruitServiceImpl implements FruitService {

    private final List<Fruit> fruits = new ArrayList<>();

    public FruitServiceImpl() {
        Fruit apple = new Fruit();
        apple.setName("Apple");
        apple.setDescription("Winter fruit");
        fruits.add(apple);

        Fruit orange = new Fruit();
        orange.setName("Orange");
        orange.setDescription("Citrus fruit");
        fruits.add(orange);
    }

    @Override
    public AddFruitResponse.Fruits addFruit(Fruit fruit) {
        fruits.add(fruit);

        AddFruitResponse.Fruits response = new AddFruitResponse.Fruits();
        response.getFruit().addAll(fruits);
        return response;
    }

    @Override
    public DeleteFruitResponse.Fruits deleteFruit(Fruit fruit) throws NoSuchFruitException {
        int index = -1;
        for (int i = 0; i < fruits.size(); i++) {
            if (fruits.get(i).getName().equals(fruit.getName())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new NoSuchFruitException("Fruit \"" + fruit.getName() + "\" does not exist.");
        }

        fruits.remove(index);

        DeleteFruitResponse.Fruits response = new DeleteFruitResponse.Fruits();
        response.getFruit().addAll(fruits);
        return response;
    }

    @Override
    public ListFruitsResponse.Fruits listFruits() {
        ListFruitsResponse.Fruits response = new ListFruitsResponse.Fruits();
        response.getFruit().addAll(fruits);
        return response;
    }
}
