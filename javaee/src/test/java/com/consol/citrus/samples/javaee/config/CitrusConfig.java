/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.samples.javaee.config;

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.xml.XsdSchemaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
@Configuration
public class CitrusConfig extends CitrusSpringConfig {

    private MailServer mailServer;
    private WebServiceServer smsGatewayServer;

    @Bean
    public XsdSchemaRepository schemaRepository() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.setLocations(Arrays.asList("classpath:wsdl/SmsGateway.wsdl"));
        schemaRepository.afterPropertiesSet();

        return schemaRepository;
    }

    @Bean
    public MailServer mailServer() {
        if (mailServer == null) {
            mailServer = new MailServer();
            mailServer.setPort(2222);
            mailServer.setAutoStart(true);
        }

        return mailServer;
    }

    @Bean
    public WebServiceServer smsGatewayServer() {
        if (smsGatewayServer == null) {
            smsGatewayServer = new WebServiceServer();
            smsGatewayServer.setPort(18008);
            smsGatewayServer.setDefaultTimeout(5000L);
            smsGatewayServer.setAutoStart(true);
        }

        return smsGatewayServer;
    }
}
