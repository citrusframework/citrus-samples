/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.bakery.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ReportService implements InitializingBean {

    /** In memory report **/
    private final Map<String, AtomicInteger> report = new LinkedHashMap<>();

    /** In memory id store of produced items */
    private final ArrayList<String> produced = new ArrayList<>();

    @Autowired
    private MailService mailService;

    /**
     * Gets the status of a very specific item found by its id.
     * @return
     */
    public String status(String id) {
        return String.format("{ \"status\": %s }", produced.contains(id));
    }

    /**
     * Adds new produced item.
     * @param id
     * @param name
     * @param amount
     */
    public void add(String id, String name, Integer amount) throws Exception {
        produced.add(id);

        if (report.containsKey(name)) {
            report.get(name).addAndGet(amount);
        } else {
            report.put(name, new AtomicInteger(amount));
        }

        if (report.get(name).get() >= 1000) {
            informStakeholders(name);
        }
    }

    /**
     * Inform stakeholders that we have produced a large amount of cookies.
     * @param name
     */
    private void informStakeholders(String name) {
        mailService.sendMail("stakeholders@example.com", "Congratulations!",
                String.format("Produced 1000+ cookies of type '%s', we are rich now!", name));
    }

    /**
     * Construct JSON report data.
     * @return
     */
    public String json() {
        return "{" +
                report.entrySet()
                        .stream()
                        .map(goods -> String.format("\"%s\": %s", goods.getKey(), goods.getValue().get()))
                        .collect(Collectors.joining(",")) +
                "}";
    }

    /**
     * Construct HTML report data.
     * @return
     */
    public String html() {
        StringBuilder response = new StringBuilder();
        response.append("<html><body><h1>Camel bakery reporting</h1><p>Today we have produced following goods:</p>");

        response.append("<ul>");

        for (Map.Entry<String, AtomicInteger> goods : report.entrySet()) {
            response.append("<li>")
                    .append(goods.getKey()).append(":").append(goods.getValue().get())
                    .append("</li>");
        }

        response.append("</ul>");

        response.append("<p><a href=\"reporting/orders\">Show orders</a></p>");
        response.append("</body></html>");

        return response.toString();
    }

    /**
     * Gets list of all produced order ids.
     * @return
     */
    public String orders() {
        StringBuilder response = new StringBuilder();
        response.append("<html><body>");

        response.append("<ul>");

        for (String orderId : produced) {
            response.append("<li>")
                    .append(orderId)
                    .append("</li>");
        }

        response.append("</ul>");
        response.append("</body></html>");

        return response.toString();
    }

    /**
     * Reset all reports in memory.
     */
    public void reset() {
        report.clear();
        produced.clear();

        report.put("chocolate", new AtomicInteger());
        report.put("caramel", new AtomicInteger());
        report.put("blueberry", new AtomicInteger());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reset();
    }
}
