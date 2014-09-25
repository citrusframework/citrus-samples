package com.consol.citrus.samples.incident;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.samples.incident.model.*;
import com.consol.citrus.validation.MarshallingValidationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageHeaders;
import org.springframework.oxm.Marshaller;
import org.testng.Assert;
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

    @Autowired
    private Marshaller jaxbMarshaller;

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
            .payload(incident, jaxbMarshaller)
            .header("SOAPAction", "/IncidentManager/openIncident");

        receive(incidentJmsEndpoint)
            .validationCallback(new MarshallingValidationCallback<OpenIncidentResponse>() {
                @Override
                public void validate(OpenIncidentResponse message, MessageHeaders headers) {
                    Assert.assertEquals(message.getTicketId(), incident.getIncident().getTicketId());
                }
            });
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
                .payload(incident, jaxbMarshaller)
                .header("SOAPAction", "/IncidentManager/openIncident");

        receive(incidentJmsEndpoint)
                .payload("<SOAP-ENV:Fault xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "<faultcode>@contains('Client')@</faultcode>" +
                            "<faultstring>@startsWith('Unmarshalling Error')@</faultstring>" +
                        "</SOAP-ENV:Fault>")
                .header("SOAPJMS_isFault", "true");
    }
}
