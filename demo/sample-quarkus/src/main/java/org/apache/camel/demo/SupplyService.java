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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Supply;
import org.apache.camel.demo.model.event.ShippingEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

@Singleton
public class SupplyService {

    private static final Logger LOG = Logger.getLogger(SupplyService.class);

    @Inject
    EntityManager em;

    @Inject
    @Channel("shipping-events")
    MutinyEmitter<ShippingEvent> shippingEmitter;

    @Inject
    ObjectMapper objectMapper;

    public Supply findById(Long id) {
        return em.createNamedQuery("Supplies.findById", Supply.class)
                        .setParameter("id", id)
                        .getSingleResult();
    }

    public Optional<Supply> findAvailable(Booking booking) {
        List<Supply> matching = em.createNamedQuery("Supplies.findMatching", Supply.class)
                        .setParameter("product", booking.getProduct().getId())
                        .setParameter("status", Supply.Status.AVAILABLE)
                        .setParameter("price", booking.getPrice())
                        .setParameter("amount", booking.getAmount())
                        .getResultList();

        if (matching.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(matching.get(0));
        }
    }

    @Transactional
    public void add(Supply supply) {
        em.persist(supply);
    }

    @Transactional
    public void update(Supply supply) {
        em.merge(supply);
    }

    public List<Supply> findAll() {
        return em.createNamedQuery("Supplies.findAll", Supply.class).getResultList();
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

    public void adjust(Supply supply, Booking booking) throws JsonProcessingException {
        supply.setAmount(supply.getAmount() - booking.getAmount());

        if (supply.getAmount() == 0) {
            supply.setStatus(Supply.Status.SOLD);
            LOG.info("Closing supply:" + objectMapper.writeValueAsString(supply));
        } else {
            LOG.info("Adjusting supply:" + objectMapper.writeValueAsString(supply));
        }

        update(supply);
        shippingEmitter.send(new ShippingEvent(booking.getClient(), supply.getProduct().getName(), booking.getAmount())).subscribe()
                .with(
                    success -> LOG.info("Shipping event successfully sent"),
                    failure -> LOG.info("Shipping event failed: " + failure.getMessage())
                );
    }
}
