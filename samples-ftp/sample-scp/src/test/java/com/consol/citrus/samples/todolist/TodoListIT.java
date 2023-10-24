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

import java.io.IOException;
import java.nio.file.Paths;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.FileUtils;
import org.apache.ftpserver.ftplet.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private ScpClient scpClient;

    @Autowired
    private SftpServer sftpServer;

    @Test
    @CitrusTest
    public void testStoreAndRetrieveFile() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(echo("Store file via SCP"));

        $(send()
            .endpoint(scpClient)
            .fork(true)
            .message(FtpMessage.put("classpath:todo/entry.json", "todo.json", DataType.ASCII)));

        $(receive()
            .endpoint(sftpServer)
            .message(FtpMessage.put("@ignore@", "todo.json", DataType.ASCII)));

        $(send()
            .endpoint(sftpServer)
            .message(FtpMessage.success()));

        $(receive()
            .endpoint(scpClient)
            .message(FtpMessage.success()));

        $(echo("Retrieve file from server"));

        $(send()
            .endpoint(scpClient)
            .fork(true)
            .message(FtpMessage.get("todo.json", "file:target/scp/todo.json", DataType.ASCII)));

        $(receive()
            .endpoint(sftpServer)
            .message(FtpMessage.get("/todo.json", "@ignore@", DataType.ASCII)));

        $(send()
            .endpoint(sftpServer)
            .message(FtpMessage.success()));

        $(receive()
            .endpoint(scpClient)
            .message(FtpMessage.success()));

        $(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                try {
                    String content = FileUtils.readToString(Paths.get("target/scp/todo.json").toFile());
                    Assert.assertEquals(content, FileUtils.readToString(Resources.fromClasspath("todo/entry.json")));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read downloaded file", e);
                }
            }
        });
    }
}
