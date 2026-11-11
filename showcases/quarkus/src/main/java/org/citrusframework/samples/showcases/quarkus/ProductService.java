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

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.apache.camel.demo.model.Product;

@Singleton
public class ProductService {

    @Inject
    EntityManager em;

    @Transactional
    public Product findById(Long id) {
        return em.createNamedQuery("Products.findById", Product.class)
                        .setParameter("id", id)
                        .getSingleResult();
    }

    @Transactional
    public Optional<Product> findByName(String name) {
        try {
            return Optional.of(em.createNamedQuery("Products.findByName", Product.class)
                    .setParameter("name", name)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void add(Product product) {
        List<Product> existing = findAll();
        if (existing.stream().anyMatch(f -> f.getName().equalsIgnoreCase(product.getName()))) {
            throw new EntityExistsException(String.format("Product with name '%s' already exists", product.getName()));
        }
        em.persist(product);
    }

    @Transactional
    public void update(Product product) {
        em.merge(product);
    }

    @Transactional
    public List<Product> findAll() {
        return em.createNamedQuery("Products.findAll", Product.class).getResultList();
    }

    @Transactional
    public boolean remove(Long id) {
        try {
            em.remove(findById(id));
        } catch(NoResultException e) {
            return false;
        }

        return true;
    }

}
