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

package com.consol.citrus.samples.incident;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import org.citrusframework.schema.samples.incidentmanager.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.UUID;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Test
public class OpenIncident_JMS_Test extends TestNGCitrusTestBuilder {

    @Autowired
    @Qualifier("incidentJmsEndpoint")
    private JmsSyncEndpoint incidentJmsEndpoint;

    @CitrusTest(name = "OpenIncident_JMS_Ok_Test")
    public void testOpenIncident_JMS_Ok() {
        final OpenIncident incident = new OpenIncident();
        incident.setIncident(new IncidentType());
        incident.getIncident().setTicketId(UUID.randomUUID().toString());
        incident.getIncident().setCaptured(Calendar.getInstance());
        incident.getIncident().setComponent(ComponentType.NETWORK);
        incident.getIncident().setState(StateType.NEW);
        incident.setCustomer(new CustomerType());
        incident.getCustomer().setId(1000);
        incident.getCustomer().setFirstname("Christoph");
        incident.getCustomer().setLastname("Deppisch");
        incident.getCustomer().setAddress("Franziskanerstr. 38, 80995 München");
        incident.getIncident().setDescription("Something went wrong!");

        send(incidentJmsEndpoint)
            .payloadModel(incident)
            .header("SOAPAction", "/IncidentManager/openIncident");

        OpenIncidentResponse response = new OpenIncidentResponse();
        response.setScheduled(TestHelper.getDefaultScheduleTime());
        response.setTicketId(incident.getIncident().getTicketId());

        receive(incidentJmsEndpoint)
            .payloadModel(response);
    }

    @CitrusTest(name = "OpenIncident_JMS_SchemaInvalid_Test")
    public void testOpenIncident_JMS_SchemaInvalid() {
        OpenIncident incident = new OpenIncident();
        incident.setIncident(new IncidentType());
        incident.getIncident().setCaptured(Calendar.getInstance());
        incident.getIncident().setComponent(ComponentType.SOFTWARE);
        incident.getIncident().setState(StateType.NEW);
        incident.getIncident().setDescription("Something missing!");

        send(incidentJmsEndpoint)
                .payloadModel(incident)
                .header("SOAPAction", "/IncidentManager/openIncident");

        receive(incidentJmsEndpoint)
                .payload("<SOAP-ENV:Fault xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "<faultcode>@contains('Client')@</faultcode>" +
                            "<faultstring>@startsWith('Unmarshalling Error')@</faultstring>" +
                        "</SOAP-ENV:Fault>")
                .header("SOAPJMS_isFault", "true");
    }

}
