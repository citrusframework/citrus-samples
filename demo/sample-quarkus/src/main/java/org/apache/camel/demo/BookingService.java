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

import java.time.Duration;
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
import org.apache.camel.demo.model.event.BookingCompletedEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

@Singleton
public class BookingService {

    private static final Logger LOG = Logger.getLogger(BookingService.class);

    @Inject
    EntityManager em;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel("completed-events")
    MutinyEmitter<BookingCompletedEvent> completedEmitter;

    public Booking findById(Long id) {
        return em.createNamedQuery("Bookings.findById", Booking.class)
                        .setParameter("id", id)
                        .getSingleResult();
    }

    public Optional<Booking> findMatching(Supply supply) {
        List<Booking> matching = em.createNamedQuery("Bookings.findMatching", Booking.class)
                        .setParameter("product", supply.getProduct().getId())
                        .setParameter("status", Booking.Status.PENDING)
                        .setParameter("price", supply.getPrice())
                        .setParameter("amount", supply.getAmount())
                        .getResultList();

        if (matching.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(matching.get(0));
        }
    }

    @Transactional
    public void add(Booking booking) {
        em.persist(booking);
    }

    @Transactional
    public void update(Booking booking) {
        em.merge(booking);
    }

    public List<Booking> findAll() {
        return em.createNamedQuery("Bookings.findAll", Booking.class).getResultList();
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

    public void complete(Booking booking) throws JsonProcessingException {
        booking.setStatus(Booking.Status.COMPLETED);
        LOG.info("Closing booking:" + objectMapper.writeValueAsString(booking));
        update(booking);
        completedEmitter.send(BookingCompletedEvent.from(booking)).await().atMost(Duration.ofSeconds(5));
    }
}
