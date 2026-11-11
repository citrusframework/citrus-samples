/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.demo;

import io.quarkus.runtime.StartupEvent;
import jakarta.inject.Inject;
import org.apache.camel.demo.model.Product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class FoodMarketApplication {

    @Inject
    ProductService productService;

    /**
     * Prepare some products.
     **/
    void onStart(@Observes StartupEvent ev) {
        productService.add(new Product("Apple", "Winter fruit"));
        productService.add(new Product("Pineapple", "Tropical fruit"));
        productService.add(new Product("Strawberry", "Delicious"));
        productService.add(new Product("Banana", "Healthy and powerful"));
        productService.add(new Product("Peach", "Delicious"));
        productService.add(new Product("Mango", "Exotic fruit"));
        productService.add(new Product("Kiwi", "Tropical fruit"));
        productService.add(new Product("Cherry", "Delicious"));
        productService.add(new Product("Orange", "Juicy fruit"));
        productService.add(new Product("Coconut", "Tropical fruit"));
    }

}
