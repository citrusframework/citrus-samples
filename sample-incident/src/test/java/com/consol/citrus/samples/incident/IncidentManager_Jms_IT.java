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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.citrusframework.schema.samples.incidentmanager.v1.*;
import org.citrusframework.schema.samples.incidentmanager.v1.IncidentType;
import org.citrusframework.schema.samples.incidentmanager.v1.StateType;
import org.citrusframework.schema.samples.networkservice.v1.*;
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
public class IncidentManager_Jms_IT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("incidentJmsEndpoint")
    private JmsSyncEndpoint incidentJmsEndpoint;

    @Autowired
    @Qualifier("networkBackendHttpServer")
    private HttpServer networkHttpServer;

    @CitrusTest(name = "IncidentManager_Jms_Ok_1_IT")
    public void testIncidentManager_Jms_Ok_1() {
        description("Calls IncidentManager application via JMS message transport using SOAP request message. Opens a new incident and verifies" +
                " proper interface calls on NetworkService as well as final incident response");

        variable("ticketId","citrus:randomUUID()");
        variable("customerId", "citrus:randomNumber(6)");

        echo("Step 1: Send OpenIncident request message to IncidentManager via Http SOAP interface");

        send(incidentJmsEndpoint)
                .fork(true)
                .payload("<im:OpenIncident xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
                            "<im:incident>" +
                                "<im:ticketId>${ticketId}</im:ticketId>" +
                                "<im:captured>citrus:currentDate('yyyy-MM-dd'T'00:00:00')</im:captured>" +
                                "<im:state>NEW</im:state>" +
                                "<im:component>SOFTWARE</im:component>" +
                                "<im:description>Something went wrong with the software!</im:description>" +
                            "</im:incident>" +
                            "<im:customer>" +
                                "<im:id>${customerId}</im:id>" +
                                "<im:firstname>Christoph</im:firstname>" +
                                "<im:lastname>Deppisch</im:lastname>" +
                                "<im:address>Franziskanerstr. 38, 80995 München</im:address>" +
                            "</im:customer>" +
                        "</im:OpenIncident>")
                .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident");

        echo("Step 2: Receive AnalyseIncident request message as NetworkService application via Http SOAP interface");

        receive(networkHttpServer)
                .payload("<net:AnalyseIncident xmlns:net=\"http://www.citrusframework.org/schema/samples/NetworkService/v1\">" +
                            "<net:incident>" +
                                "<net:ticketId>${ticketId}</net:ticketId>" +
                                "<net:description>Something went wrong with the software!</net:description>" +
                            "</net:incident>" +
                            "<net:network>" +
                                "<net:lineId>@ignore@</net:lineId>" +
                                "<net:type>SOFTWARE</net:type>" +
                                "<net:connection>@ignore@</net:connection>" +
                            "</net:network>" +
                        "</net:AnalyseIncident>")
                .extractFromPayload("net:AnalyseIncident/net:network/net:lineId", "lineId")
                .extractFromPayload("net:AnalyseIncident/net:network/net:connection" ,"connectionId");

        echo("Step:3 Send AnalyseIncidentResponse message as result of the NetworkService call");

        send(networkHttpServer)
                .payload("<net:AnalyseIncidentResponse xmlns:net=\"http://www.citrusframework.org/schema/samples/NetworkService/v1\">" +
                            "<net:ticketId>${ticketId}</net:ticketId>" +
                            "<net:result>" +
                                "<net:lineId>${lineId}</net:lineId>" +
                                "<net:resultCode>CODE_citrus:randomNumber(4)</net:resultCode>" +
                                "<net:solved>true</net:solved>" +
                                "<net:bandwidth>12000</net:bandwidth>" +
                                "<net:lineCheck>OK</net:lineCheck>" +
                                "<net:connectionCheck>OK</net:connectionCheck>" +
                                "<net:fieldForceRequired>false</net:fieldForceRequired>" +
                            "</net:result>" +
                        "</net:AnalyseIncidentResponse>")
                .header("Content-Type", "application/xml");

        echo("Step 4: Receive OpenIncident response message with analyse outcome from IncidentManager application");

        receive(incidentJmsEndpoint)
                .payload("<im:OpenIncidentResponse xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
                            "<im:ticketId>${ticketId}</im:ticketId>" +
                            "<im:scheduled>@ignore@</im:scheduled>" +
                        "</im:OpenIncidentResponse>");
    }

    @CitrusTest(name = "IncidentManager_Jms_Ok_2_IT")
    public void testIncidentManager_Jms_Ok_2() {
        OpenIncident incident = createOpenIncidentTestRequest();
        send(incidentJmsEndpoint)
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
        receive(incidentJmsEndpoint)
            .payloadModel(response);
    }

    @CitrusTest(name = "IncidentManager_Jms_SchemaInvalid_IT")
    public void testIncidentManager_Jms_SchemaInvalid() {
        OpenIncident incident = new OpenIncident();
        incident.setIncident(new IncidentType());
        incident.getIncident().setCaptured(Calendar.getInstance());
        incident.getIncident().setComponent(ComponentType.SOFTWARE);
        incident.getIncident().setState(StateType.NEW);
        incident.getIncident().setDescription("Something missing!");

        send(incidentJmsEndpoint)
                .payloadModel(incident)
                .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident");

        receive(incidentJmsEndpoint)
                .payload("<SOAP-ENV:Fault xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "<faultcode>@contains('Client')@</faultcode>" +
                            "<faultstring>@startsWith('Unmarshalling Error')@</faultstring>" +
                        "</SOAP-ENV:Fault>")
                .header("SOAPJMS_isFault", "true");
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

        return  analyseIncident;
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

        return response;
    }

}
