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

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

@Entity
@NamedQueries({
        @NamedQuery(name = "Supplies.findById",
                query = "SELECT s FROM Supply s WHERE s.id = :id"),
        @NamedQuery(name = "Supplies.findMatching",
                query = "SELECT s FROM Supply s WHERE s.product.id = :product AND s.status = :status AND s.price <= :price AND s.amount >= :amount ORDER BY s.price"),
        @NamedQuery(name = "Supplies.findAll",
                query = "SELECT s FROM Supply s ORDER BY s.id")
})
public class Supply {

    private Long id;
    private String supplier;
    private Product product;
    private Integer amount;
    private BigDecimal price = new BigDecimal("0.0");

    private Status status = Status.AVAILABLE;

    public Supply() {
    }

    public Supply(String supplier, Product product, int amount, double price) {
        this.supplier = supplier;
        this.product = product;
        this.amount = amount;
        this.price = BigDecimal.valueOf(price);
    }

    public enum Status {
        AVAILABLE,
        SOLD
    }

    @Id
    @SequenceGenerator(name = "supSeq", sequenceName = "sup_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "supSeq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    @ManyToOne(optional = false)
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
