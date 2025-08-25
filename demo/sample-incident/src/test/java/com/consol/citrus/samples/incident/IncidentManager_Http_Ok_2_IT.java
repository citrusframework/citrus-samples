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

package com.consol.citrus.samples.incident;

import org.apache.hc.core5.http.ContentType;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class IncidentManager_Http_Ok_2_IT extends TestNGCitrusSpringSupport implements TestActionSupport {

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

    @Test
    @CitrusTest(name = "IncidentManager_Http_Ok_2_IT")
    public void testIncidentManager_Http_Ok_2() {
        description("Calls IncidentManager application via Http message transport using SOAP request message. Opens a new incident and verifies" +
                " proper interface calls on NetworkService as well as final incident response. This time field force interaction is necessary");

        variable("ticketId","citrus:randomUUID()");
        variable("customerId", "citrus:randomNumber(6)");

        $(echo("Step 1: Send OpenIncident request message to IncidentManager via Http SOAP interface"));

        $(send()
            .endpoint(incidentHttpClient)
            .fork(true)
            .message()
            .body("<im:OpenIncident xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
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
            .header(SoapMessageHeaders.SOAP_ACTION, "/IncidentManager/openIncident"));

        $(echo("Step 2: Receive AnalyseIncident request message as NetworkService application via Http SOAP interface"));

        $(receive()
            .endpoint(networkHttpServer)
            .message()
            .body("<net:AnalyseIncident xmlns:net=\"http://www.citrusframework.org/schema/samples/NetworkService/v1\">" +
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
            .extract(extractor().xpath()
                    .expression("net:AnalyseIncident/net:network/net:lineId", "lineId")
                    .expression("net:AnalyseIncident/net:network/net:connection" ,"connectionId")));

        $(echo("Step:3 Send AnalyseIncidentResponse message as result of the NetworkService call"));

        $(send()
            .endpoint(networkHttpServer)
            .message()
            .body("<net:AnalyseIncidentResponse xmlns:net=\"http://www.citrusframework.org/schema/samples/NetworkService/v1\">" +
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
            .header("Content-Type", ContentType.APPLICATION_XML.getMimeType()));

        $(echo("Step 4: Receive OpenIncident response message with analyse outcome from IncidentManager application"));

        $(receive()
            .endpoint(incidentHttpClient)
            .message()
            .body("<im:OpenIncidentResponse xmlns:im=\"http://www.citrusframework.org/schema/samples/IncidentManager/v1\">" +
                       "<im:ticketId>${ticketId}</im:ticketId>" +
                       "<im:scheduled>@ignore@</im:scheduled>" +
                     "</im:OpenIncidentResponse>"));

        $(echo("Step 5: Receive FieldForce Order request"));

        $(receive()
            .endpoint(fieldForceOrderEndpoint)
            .message()
            .body("<ffs:OrderRequest xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
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
                     "</ffs:OrderRequest>"));

        $(echo("Step 6: Send FieldForce notifications"));

        $(echo("Step 6.1: Send NEW field force notification"));

        $(send()
            .endpoint(fieldForceNotificationEndpoint)
            .message()
            .body("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                       "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                       "<ffs:customerId>${customerId}</ffs:customerId>" +
                       "<ffs:state>NEW</ffs:state>" +
                     "</ffs:OrderNotification>"));

        $(sleep().milliseconds(500L));

        $(echo("Step 6.2: Handle ON_SITE field force notification"));

        $(send()
            .endpoint(fieldForceNotificationEndpoint)
            .message()
            .body("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                       "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                       "<ffs:customerId>${customerId}</ffs:customerId>" +
                       "<ffs:state>ON_SITE</ffs:state>" +
                     "</ffs:OrderNotification>"));

        $(receive()
            .endpoint(smsGatewayServer)
            .message()
            .body("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                       "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                       "<sms:customerId>${customerId}</sms:customerId>" +
                       "<sms:text>News from ticket '${ticketId}' - we started to fix your problem!</sms:text>" +
                     "</sms:SendSmsRequest>"));

        $(send()
            .endpoint(smsGatewayServer)
            .message()
            .body("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                       "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                       "<sms:success>true</sms:success>" +
                     "</sms:SendSmsResponse>"));

        $(sleep().milliseconds(1500L));

        $(echo("Step 6.3: Handle FIXED field force notification"));

        $(send()
            .endpoint(fieldForceNotificationEndpoint)
            .message()
            .body("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                       "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                       "<ffs:customerId>${customerId}</ffs:customerId>" +
                       "<ffs:state>FIXED</ffs:state>" +
                       "<ffs:reason>LOCATION_NOT_FOUND</ffs:reason>" +
                     "</ffs:OrderNotification>"));

        $(receive()
            .endpoint(smsGatewayServer)
            .message()
            .body("<sms:SendSmsRequest xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                       "<sms:communicationId>@variable('smsCommunicationId')@</sms:communicationId>" +
                       "<sms:customerId>${customerId}</sms:customerId>" +
                       "<sms:text>News from ticket '${ticketId}' - your problem is solved!</sms:text>" +
                     "</sms:SendSmsRequest>"));

        $(send()
            .endpoint(smsGatewayServer)
            .message()
            .body("<sms:SendSmsResponse xmlns:sms=\"http://www.citrusframework.org/schema/samples/SmsGateway/v1\">" +
                       "<sms:communicationId>${smsCommunicationId}</sms:communicationId>" +
                       "<sms:success>true</sms:success>" +
                     "</sms:SendSmsResponse>"));

        $(sleep().milliseconds(1000L));

        $(echo("Step 6.4: Send CLOSED field force notification"));

        $(send()
            .endpoint(fieldForceNotificationEndpoint)
            .message()
            .body("<ffs:OrderNotification xmlns:ffs=\"http://www.citrusframework.org/schema/samples/FieldForceService/v1\">" +
                       "<ffs:ticketId>${ticketId}</ffs:ticketId>" +
                       "<ffs:customerId>${customerId}</ffs:customerId>" +
                       "<ffs:state>CLOSED</ffs:state>" +
                     "</ffs:OrderNotification>"));

    }
}
