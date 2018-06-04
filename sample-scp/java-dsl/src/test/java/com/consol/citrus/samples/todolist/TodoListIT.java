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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.client.ScpClient;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.server.SftpServer;
import com.consol.citrus.util.FileUtils;
import org.apache.ftpserver.ftplet.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private ScpClient scpClient;

    @Autowired
    private SftpServer sftpServer;

    @Test
    @CitrusTest
    public void testStoreAndRetrieveFile() {
        echo("Store file via SCP");

        send(scpClient)
           .fork(true)
           .message(FtpMessage.put("classpath:todo/entry.json", "todo.json", DataType.ASCII));

        receive(sftpServer)
            .message(FtpMessage.put("@ignore@", "todo.json", DataType.ASCII));

        send(sftpServer)
            .message(FtpMessage.success());

        receive(scpClient)
            .message(FtpMessage.success());

        echo("Retrieve file from server");

        send(scpClient)
            .fork(true)
            .message(FtpMessage.get("todo.json", "file:target/scp/todo.json", DataType.ASCII));

        receive(sftpServer)
            .message(FtpMessage.get("/todo.json", "@ignore@", DataType.ASCII));

        send(sftpServer)
            .message(FtpMessage.success());

        receive(scpClient)
            .message(FtpMessage.success());

        action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                try {
                    String content = FileUtils.readToString(Paths.get("target/scp/todo.json").toFile());
                    Assert.assertEquals(content, FileUtils.readToString(new ClassPathResource("todo/entry.json")));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read downloaded file", e);
                }
            }
        });
    }
}
