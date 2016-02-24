/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.samples.javaee.employee.jms;

import com.consol.citrus.samples.javaee.employee.EmployeeRepository;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty( propertyName = "destination", propertyValue = "jms/queue/employee"),
        @ActivationConfigProperty( propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class EmployeeJmsResource implements MessageListener {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(EmployeeJmsResource.class);

    @EJB
    private EmployeeRepository bean;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory factory;

    @Override
    public void onMessage(Message msg) {
        try
        {
            log.info("Received JMS request " + msg.getJMSMessageID());

            Employee employee = new Employee(msg.getStringProperty("name"), msg.getIntProperty("age"), msg.getStringProperty("email"));
            bean.addEmployee(employee);

            Connection connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(msg.getJMSReplyTo());

            TextMessage reply = session.createTextMessage("Successfully created employee: " + employee.toString());
            reply.setBooleanProperty("success", true);

            producer.send(reply);
            producer.close();
            session.close();
            connection.close();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not reply to JMS message", e);
        }
    }
}
