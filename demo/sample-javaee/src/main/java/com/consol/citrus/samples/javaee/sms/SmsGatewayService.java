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

package com.consol.citrus.samples.javaee.sms;

import org.apache.cxf.feature.LoggingFeature;
import org.smsgateway.schema.smsgateway.v1.*;

import javax.ejb.Singleton;
import javax.xml.ws.BindingProvider;
import java.util.UUID;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
@Singleton
public class SmsGatewayService {

    private SmsGateway smsGatewayClient;

    public SmsGatewayService() {
        this.smsGatewayClient = new SmsGateway_Service(ClassLoader.getSystemResource("wsdl/SmsGateway.wsdl"), SmsGateway_Service.SERVICE, new LoggingFeature())
                .getSmsGatewayHTTP();

        ((BindingProvider) smsGatewayClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:18008/sms/SmsGateway/v1");
    }

    public boolean sendSms(String msisdn, String text) {
        try {
            SendSmsRequest request = new SendSmsRequest();
            request.setCommunicationId(UUID.randomUUID().toString());
            request.setMsisdn(Long.valueOf(msisdn));
            request.setText(text);

            SendSmsResponse response = smsGatewayClient.sendSms(request);

            if (response != null) {
                return response.isSuccess();
            } else {
                return false;
            }
        } catch (SmsFault_Exception fault) {
            throw new RuntimeException(fault);
        }
    }
}
