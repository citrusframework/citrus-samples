package com.consol.citrus.samples.incident;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.samples.incident.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.testng.annotations.Test;

import java.util.Calendar;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Test
public class OpenIncident_Test extends TestNGCitrusTestBuilder {

    @Autowired
    private JmsEndpoint incidentManagerEndpointJMS;

    @Autowired
    private Marshaller jaxbMarshaller;

    @CitrusTest(name = "OpenIncident_Ok_Test")
    public void testOpenIncident() {

        OpenIncident incident = new OpenIncident();
        incident.setIncident(new IncidentType());
        incident.getIncident().setTicketId("1234");
        incident.getIncident().setCaptured(Calendar.getInstance());
        incident.getIncident().setComponent(ComponentType.NETWORK);
        incident.getIncident().setState(StateType.NEW);
        incident.setCustomer(new CustomerType());
        incident.getCustomer().setId("1000");
        incident.getCustomer().setFirstname("Christoph");
        incident.getCustomer().setLastname("Deppisch");
        incident.getCustomer().setAddress("Franziskanerstr. 38, 80995 München");

        send(incidentManagerEndpointJMS)
            .payload(incident, jaxbMarshaller)
            .header("citrus_soap_action", "/IncidentManager/openIncident");
    }
}
