package com.consol.citrus.samples.incident;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.functions.core.RandomUUIDFunction;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import com.consol.citrus.ws.server.WebServiceServer;
import org.apache.http.entity.ContentType;
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

    @Autowired
    @Qualifier("smsGatewayServer")
    private WebServiceServer smsGatewayServer;

    @Autowired
    @Qualifier("fieldForceOrderEndpoint")
    private JmsEndpoint fieldForceOrderEndpoint;

    @Autowired
    @Qualifier("fieldForceNotificationEndpoint")
    private JmsEndpoint fieldForceNotificationEndpoint;

    @CitrusTest(name = "IncidentManager_Http_FieldForceError_1_IT")
    public void testIncidentManager_Http_FieldForceError_1() {
        description("Calls IncidentManager application via Http message transport using SOAP request message. Opens a new incident and verifies" +
                " proper interface calls on NetworkService as well as final incident response. This time field force interaction is necessary and is" +
                " aborted because of customer not found error");

        variable("ticketId","citrus:randomUUID()");
        variable("customerId", "citrus:randomNumber(6)");

        echo("Step 1: Send OpenIncident request message to IncidentManager via Http SOAP interface");

        send(incidentHttpClient)
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
                                "<net:description>Something went wrong with the software!</net:description></net:incident>" +
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
                                "<net:solved>false</net:solved>" +
                                "<net:bandwidth>12000</net:bandwidth>" +
                                "<net:lineCheck>OK</net:lineCheck>" +
                                "<net:connectionCheck>OK</net:connectionCheck>" +
                                "<net:fieldForceRequired>true</net:fieldForceRequired>" +
                            "</net:result>" +
                        "</net:AnalyseIncidentResponse>")
                .header("Content-Type", ContentType.APPLICATION_XML.getMimeType());

        echo("Step 4: Receive OpenIncident response message with analyse outcome from IncidentManager application");

        receive(incidentHttpClient)
                .payload("<im:OpenIncidentResponse xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
                        "<im:ticketId>${ticketId}</im:ticketId>" +
                        "<im:scheduled>@ignore@</im:scheduled>" +
                        "</im:OpenIncidentResponse>");

        echo("Step 5: Receive FieldForce Order request");

        receive(fieldForceOrderEndpoint)
                .payload("<ffs:OrderRequest xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:incident>" +
                                "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                                "<ffs:description>@contains('Something went wrong')@</ffs:description>" +
                            "</ffs:incident>" +
                            "<ffs:customer>" +
                                "<ffs:id>${customerId}</ffs:id>" +
                                "<ffs:firstname>Christoph</ffs:firstname>" +
                                "<ffs:lastname>Deppisch</ffs:lastname>" +
                                "<ffs:address>Franziskanerstr. 38, 80995 München</ffs:address>" +
                            "</ffs:customer>" +
                            "<ffs:network>" +
                                "<ffs:lineId>${lineId}</ffs:lineId>" +
                            "</ffs:network>" +
                        "</ffs:OrderRequest>");

        echo("Step 6: Send FieldForce notifications");

        echo("Step 6.1: Send NEW field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>NEW</ffs:state>" +
                        "</ffs:OrderNotification>");

        sleep(500L);

        echo("Step 6.2: Handle ON_SITE field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>ON_SITE</ffs:state>" +
                        "</ffs:OrderNotification>");

        receive(smsGatewayServer)
                .payload("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                            "<sms:customerId>${customerId}</sms:customerId>" +
                            "<sms:text>News from ticket '${ticketId}' - we started to fix your problem!</sms:text>" +
                        "</sms:SendSmsRequest>");

        send(smsGatewayServer)
                .payload("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                            "<sms:success>true</sms:success>" +
                        "</sms:SendSmsResponse>");

        sleep(1500L);

        echo("Step 6.3: Handle ABORTED field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>ABORTED</ffs:state>" +
                            "<ffs:reason>CUSTOMER_NOT_FOUND</ffs:reason>" +
                        "</ffs:OrderNotification>");

        receive(smsGatewayServer)
                .payload("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                            "<sms:customerId>${customerId}</sms:customerId>" +
                            "<sms:text>News from ticket '${ticketId}' - we stopped processing your issue! Reason: CUSTOMER_NOT_FOUND</sms:text>" +
                        "</sms:SendSmsRequest>");

        send(smsGatewayServer)
                .payload("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                            "<sms:success>true</sms:success>" +
                        "</sms:SendSmsResponse>");
    }

    @CitrusTest(name = "IncidentManager_Http_FieldForceError_2_IT")
    public void testIncidentManager_Http_FieldForceError_2() {
        description("Calls IncidentManager application via Http message transport using SOAP request message. Opens a new incident and verifies" +
                " proper interface calls on NetworkService as well as final incident response. This time field force interaction is necessary and is" +
                " aborted because of location not found error");

        variable("ticketId","citrus:randomUUID()");
        variable("customerId", "citrus:randomNumber(6)");

        echo("Step 1: Send OpenIncident request message to IncidentManager via Http SOAP interface");

        send(incidentHttpClient)
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
                                "<net:description>Something went wrong with the software!</net:description></net:incident>" +
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
                                "<net:solved>false</net:solved>" +
                                "<net:bandwidth>12000</net:bandwidth>" +
                                "<net:lineCheck>OK</net:lineCheck>" +
                                "<net:connectionCheck>OK</net:connectionCheck>" +
                                "<net:fieldForceRequired>true</net:fieldForceRequired>" +
                            "</net:result>" +
                        "</net:AnalyseIncidentResponse>")
                .header("Content-Type", ContentType.APPLICATION_XML.getMimeType());

        echo("Step 4: Receive OpenIncident response message with analyse outcome from IncidentManager application");

        receive(incidentHttpClient)
                .payload("<im:OpenIncidentResponse xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
                            "<im:ticketId>${ticketId}</im:ticketId>" +
                            "<im:scheduled>@ignore@</im:scheduled>" +
                        "</im:OpenIncidentResponse>");

        echo("Step 5: Receive FieldForce Order request");

        receive(fieldForceOrderEndpoint)
                .payload("<ffs:OrderRequest xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                        "<ffs:incident>" +
                                "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                                "<ffs:description>@contains('Something went wrong')@</ffs:description>" +
                            "</ffs:incident>" +
                            "<ffs:customer>" +
                                "<ffs:id>${customerId}</ffs:id>" +
                                "<ffs:firstname>Christoph</ffs:firstname>" +
                                "<ffs:lastname>Deppisch</ffs:lastname>" +
                                "<ffs:address>Franziskanerstr. 38, 80995 München</ffs:address>" +
                            "</ffs:customer>" +
                            "<ffs:network>" +
                                "<ffs:lineId>${lineId}</ffs:lineId>" +
                            "</ffs:network>" +
                        "</ffs:OrderRequest>");

        echo("Step 6: Send FieldForce notifications");

        echo("Step 6.1: Send NEW field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>NEW</ffs:state>" +
                        "</ffs:OrderNotification>");

        sleep(500L);

        echo("Step 6.2: Handle ON_SITE field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>ON_SITE</ffs:state>" +
                        "</ffs:OrderNotification>");

        receive(smsGatewayServer)
                .payload("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                            "<sms:customerId>${customerId}</sms:customerId>" +
                            "<sms:text>News from ticket '${ticketId}' - we started to fix your problem!</sms:text>" +
                        "</sms:SendSmsRequest>");

        send(smsGatewayServer)
                .payload("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                            "<sms:success>true</sms:success>" +
                        "</sms:SendSmsResponse>");

        sleep(1500L);

        echo("Step 6.3: Handle ABORTED field force notification");

        send(fieldForceNotificationEndpoint)
                .payload("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                            "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                            "<ffs:customerId>${customerId}</ffs:customerId>" +
                            "<ffs:state>ABORTED</ffs:state>" +
                            "<ffs:reason>LOCATION_NOT_FOUND</ffs:reason>" +
                        "</ffs:OrderNotification>");

        receive(smsGatewayServer)
                .payload("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                            "<sms:customerId>${customerId}</sms:customerId>" +
                            "<sms:text>News from ticket '${ticketId}' - we stopped processing your issue! Reason: LOCATION_NOT_FOUND</sms:text>" +
                        "</sms:SendSmsRequest>");

        send(smsGatewayServer)
                .payload("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                            "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                            "<sms:success>true</sms:success>" +
                        "</sms:SendSmsResponse>");
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
                .header("Content-Type", ContentType.APPLICATION_XML.getMimeType());

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
                .header("Content-Type", ContentType.APPLICATION_XML.getMimeType());

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
