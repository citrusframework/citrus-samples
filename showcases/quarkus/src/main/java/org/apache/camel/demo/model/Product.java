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

package org.apache.camel.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

@Entity
@NamedQueries({
    @NamedQuery(name = "Products.findById",
            query = "SELECT f FROM Product f WHERE f.id = :id"),
    @NamedQuery(name = "Products.findByName",
            query = "SELECT f FROM Product f WHERE f.name = :name"),
    @NamedQuery(name = "Products.findAll",
            query = "SELECT f FROM Product f ORDER BY f.id")
})
public class Product {

    private Long id;
    private String name;
    private String description;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    public Product(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Id
    @SequenceGenerator(name = "prodSeq", sequenceName = "prod_id_seq", allocationSize = 1, initialValue = 1000)
    @GeneratedValue(generator = "prodSeq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
