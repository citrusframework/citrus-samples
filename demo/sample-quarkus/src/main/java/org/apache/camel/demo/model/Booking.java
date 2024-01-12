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
        @NamedQuery(name = "Bookings.findById",
                query = "SELECT b FROM Booking b WHERE b.id = :id"),
        @NamedQuery(name = "Bookings.findMatching",
                query = "SELECT b FROM Booking b WHERE b.product.id = :product AND b.status = :status AND b.price >= :price AND b.amount <= :amount ORDER BY b.price DESC"),
        @NamedQuery(name = "Bookings.findAll",
                query = "SELECT b FROM Booking b ORDER BY b.id")
})
public class Booking {

    private Long id;
    private String client;
    private Product product;
    private Integer amount;
    private BigDecimal price = new BigDecimal("0.0");

    private String shippingAddress;

    private Status status = Status.PENDING;

    public Booking() {
    }

    public Booking(String client, Product product, int amount, double price) {
        this(client, product, amount, price, null);
    }

    public Booking(String client, Product product, int amount, double price, String shippingAddress) {
        this.client = client;
        this.product = product;
        this.amount = amount;
        this.price = BigDecimal.valueOf(price);
        this.shippingAddress = shippingAddress;
    }

    public enum Status {
        PENDING,
        COMPLETED
    }

    @Id
    @SequenceGenerator(name = "bkSeq", sequenceName = "bk_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "bkSeq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
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

    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
