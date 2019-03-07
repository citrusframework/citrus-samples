/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.ftp.client.FtpClient;
import com.consol.citrus.ftp.server.FtpServer;
import org.apache.commons.net.ftp.FTPCmd;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class EndpointConfig {

    @Bean
    public FtpClient ftpClient() {
        return CitrusEndpoints
            .ftp()
                .client()
                .autoReadFiles(true)
                .port(22222)
                .username("citrus")
                .password("admin")
                .timeout(10000L)
            .build();
    }

    @Bean
    public FtpServer ftpListServer() {
        return CitrusEndpoints
            .ftp()
                .server()
                .port(22222)
                .autoLogin(true)
                .autoStart(true)
                .autoHandleCommands(Stream.of(FTPCmd.MKD.getCommand(),
                                              FTPCmd.PORT.getCommand(),
                                              FTPCmd.PASV.getCommand(),
                                              FTPCmd.TYPE.getCommand()).collect(Collectors.joining(",")))
                .userManagerProperties(new ClassPathResource("citrus.ftp.user.properties"))
            .build();
    }
}
