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

import java.io.IOException;

import com.consol.citrus.samples.incident.exception.ServiceException;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import org.citrusframework.schema.samples.fieldforceservice.v1.OrderNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.xml.transform.StringSource;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FieldForceJmsConsumer implements MessageListener {

    @Autowired
    private FieldForceService fieldForceService;

    @Autowired
    private Unmarshaller unmarshaller;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                OrderNotification notification = (OrderNotification) unmarshaller.unmarshal(new StringSource(((TextMessage) message).getText()));
                fieldForceService.process(notification);
            }
        } catch (JMSException e) {
            throw new ServiceException("Failed to convert field force notification message", e);
        } catch (IOException e) {
            throw new ServiceException("Failed to read field force notification message", e);
        }
    }
}
