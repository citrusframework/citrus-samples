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

import com.consol.citrus.samples.incident.exception.ServiceException;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.oxm.Marshaller;
import org.springframework.xml.transform.StringResult;

import javax.jms.*;
import java.io.IOException;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FieldForceJmsClient {

    /** JMS connecting template */
    private final JmsTemplate jmsTemplate;

    @Autowired
    private Marshaller marshaller;

    /**
     * Default constructor using JMS message template.
     * @param jmsTemplate
     */
    public FieldForceJmsClient(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Sends new order request via JMS.
     * @param order
     */
    public void send(final OrderRequest order) {
        jmsTemplate.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage();

                StringResult payload = new StringResult();
                try {
                    marshaller.marshal(order, payload);
                } catch (IOException e) {
                    throw new ServiceException("Failed to create field force order request message", e);
                }

                textMessage.setText(payload.toString());
                return textMessage;
            }
        });
    }
}
