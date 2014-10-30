/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.samples.incident.service;

import org.citrusframework.schema.samples.fieldforceservice.v1.OrderNotification;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderRequest;
import org.citrusframework.schema.samples.smsgateway.v1.SendSmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FieldForceServiceImpl implements FieldForceService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FieldForceServiceImpl.class);

    /** Client producing JMS request */
    @Autowired
    private FieldForceJmsClient fieldForceClient;

    @Autowired
    private SmsService smsService;

    /** State that should trigger SMS message sending */
    private List<String> smsSendStates = Arrays.asList("ON_SITE", "FIXED", "ABORTED");

    @Override
    public void placeOrder(OrderRequest order) {
        fieldForceClient.send(order);
    }

    @Override
    public void process(OrderNotification notification) {
        log.info(String.format("Received FieldForce order notification for ticket '%s' ", notification.getTicketId()));

        if (smsSendStates.contains(notification.getState())) {
            SendSmsRequest sms = new SendSmsRequest();
            sms.setCommunicationId(UUID.randomUUID().toString());
            sms.setCustomerId(notification.getCustomerId());

            switch (notification.getState()) {
                case "ON_SITE":
                    sms.setText(String.format("News from ticket '%s' - we started to fix your problem!", notification.getTicketId()));
                    break;
                case "FIXED":
                    sms.setText(String.format("News from ticket '%s' - your problem is solved!", notification.getTicketId()));
                    break;
                case "ABORTED":
                    sms.setText(String.format("News from ticket '%s' - we stopped processing your issue! Reason: %s", notification.getTicketId(), notification.getReason().value()));
                    break;
                default:
                    sms.setText(String.format("News from ticket '%s' - %s", notification.getTicketId(), notification.getState()));
            }


            log.info(String.format("Send SMS message for field force notification state '%s'", notification.getState()));
            if (!smsService.sendSms(sms)) {
                log.warn("Send SMS failed!");
            }
        } else {
            log.info(String.format("Ignore field force notification state '%s'", notification.getState()));
        }
    }
}
