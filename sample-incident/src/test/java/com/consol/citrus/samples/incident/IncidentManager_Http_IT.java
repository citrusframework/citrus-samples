package com.consol.citrus.samples.incident;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.functions.core.RandomUUIDFunction;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.citrusframework.schema.samples.incidentmanager.v1.*;
import org.citrusframework.schema.samples.incidentmanager.v1.IncidentType;
import org.citrusframework.schema.samples.incidentmanager.v1.StateType;
import org.citrusframework.schema.samples.networkservice.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Test
public class IncidentManager_Http_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("incidentHttpClient")
    private WebServiceClient incidentHttpClient;

    @Autowired
    @Qualifier("networkBackendHttpServer")
    private HttpServer networkHttpServer;

    @CitrusXmlTest(name = "IncidentManager_Http_FieldForceError_1_IT")
    public void testIncidentManager_Http_FieldForceError_1() {
    }

    @CitrusXmlTest(name = "IncidentManager_Http_FieldForceError_2_IT")
    public void testIncidentManager_Http_FieldForceError_2() {
    }

    @CitrusTest(name = "IncidentManager_Http_Ok_3_IT")
    public void testIncidentManager_Http_Ok_3() {
        OpenIncident incident = createOpenIncidentTestRequest();
        send(incidentHttpClient)
                .fork(true)
                .payloadModel(incident)
                .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident");


        AnalyseIncident analyseIncident = createAnalyseIncidentTestRequest(incident);
        receive(networkHttpServer)
                .payloadModel(analyseIncident)
                .extractFromPayload("net:AnalyseIncident/net:network/net:lineId", "lineId")
                .extractFromPayload("net:AnalyseIncident/net:network/net:connection", "connection");

        AnalyseIncidentResponse analyseIncidentResponse = createAnalyseIncidentTestResponse(incident);
        send(networkHttpServer)
                .payloadModel(analyseIncidentResponse)
                .header("Content-Type", "application/xml");

        OpenIncidentResponse response = createOpenIncidentTestResponse(incident);
        receive(incidentHttpClient)
                .payloadModel(response);
    }

    private Resource incidentRequest = new ClassPathResource("templates/IncidentRequest.xml");
    private Resource analyseRequest = new ClassPathResource("templates/AnalyseRequest.xml");
    private Resource analyseResponse = new ClassPathResource("templates/AnalyseResponse.xml");
    private Resource incidentResponse = new ClassPathResource("templates/IncidentResponse.xml");

    @CitrusTest(name = "IncidentManager_Http_Ok_4_IT")
    public void testIncidentManager_Http_Ok_4 () {
        variable("ticketId", new RandomUUIDFunction().execute(Collections.<String>emptyList(), null));
        variable("customerId", new RandomNumberFunction().execute(Collections.<String>singletonList("6"), null));

        send(incidentHttpClient)
                .fork(true)
                .payload(incidentRequest)
                .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident");

        receive(networkHttpServer)
                .payload(analyseRequest)
                .extractFromPayload("net:AnalyseIncident/net:network/net:lineId", "lineId")
                .extractFromPayload("net:AnalyseIncident/net:network/net:connection", "connectionId");

        send(networkHttpServer)
                .payload(analyseResponse)
                .header("Content-Type", "application/xml");

        receive(incidentHttpClient)
                .payload(incidentResponse);
    }

    @CitrusTest(name = "IncidentManager_Http_SchemaInvalid_IT")
    public void testIncidentManager_Http_SchemaInvalid() {
        OpenIncident incident = new OpenIncident();
        incident.setIncident(new IncidentType());
        incident.getIncident().setCaptured(Calendar.getInstance());
        incident.getIncident().setComponent(ComponentType.SOFTWARE);
        incident.getIncident().setState(StateType.NEW);
        incident.getIncident().setDescription("Something missing!");

        assertSoapFault()
                .faultCode("{http://schemas.xmlsoap.org/soap/envelope/}Client").faultString("@startsWith('Unmarshalling Error')@")
                .when(send(incidentHttpClient)
                        .payloadModel(incident)
                        .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident"));
    }

    private OpenIncident createOpenIncidentTestRequest() {
        OpenIncident incident = new OpenIncident();
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

        return incident;
    }

    private AnalyseIncident createAnalyseIncidentTestRequest(OpenIncident incident) {
        AnalyseIncident analyseIncident = new AnalyseIncident();
        analyseIncident.setIncident(new org.citrusframework.schema.samples.networkservice.v1.IncidentType());
        analyseIncident.getIncident().setTicketId(incident.getIncident().getTicketId());
        analyseIncident.getIncident().setDescription(incident.getIncident().getDescription());
        analyseIncident.setNetwork(new NetworkType());
        analyseIncident.getNetwork().setType(NetworkComponentType.valueOf(incident.getIncident().getComponent().name()));
        analyseIncident.getNetwork().setLineId("@ignore@");
        analyseIncident.getNetwork().setConnection("@ignore@");

        return analyseIncident;
    }

    private AnalyseIncidentResponse createAnalyseIncidentTestResponse(OpenIncident incident) {
        AnalyseIncidentResponse analyseIncidentResponse = new AnalyseIncidentResponse();
        analyseIncidentResponse.setTicketId(incident.getIncident().getTicketId());
        analyseIncidentResponse.setResult(new AnalyseIncidentResultType());
        analyseIncidentResponse.getResult().setLineId("${lineId}");
        analyseIncidentResponse.getResult().setBandwidth(12000);
        analyseIncidentResponse.getResult().setLineCheck(CheckType.OK);
        analyseIncidentResponse.getResult().setConnectionCheck(CheckType.OK);
        analyseIncidentResponse.getResult().setFieldForceRequired(false);
        analyseIncidentResponse.getResult().setResultCode("CODE_citrus:randomNumber(4)");
        analyseIncidentResponse.getResult().setSolved(true);

        return analyseIncidentResponse;
    }

    private OpenIncidentResponse createOpenIncidentTestResponse(OpenIncident incident) {
        OpenIncidentResponse response = new OpenIncidentResponse();
        response.setScheduled(TestHelper.getDefaultScheduleTime());
        response.setTicketId(incident.getIncident().getTicketId());

        return  response;
    }
}
