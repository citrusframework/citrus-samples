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
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.server.SftpServer;
import com.consol.citrus.util.FileUtils;
import org.apache.ftpserver.ftplet.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private SftpServer sftpServer;

    private Resource privateKey = new ClassPathResource("ssh/citrus.priv");

    @Test
    @CitrusTest
    public void testStoreAndRetrieveFile() throws IOException {
        String sourcePath = new ClassPathResource("todo/entry.json").getFile().getAbsolutePath();
        String targetFile = "todo.json";
        String downloadPath = "target/scp/" + targetFile;

        echo("Store file via SCP");

        async().actions(new ScpClientAction(sftpServer.getUser(), "localhost", sftpServer.getPort(), privateKey.getFile().getAbsolutePath())
                                .upload(sourcePath, targetFile));

        receive(sftpServer)
                .message(FtpMessage.put("@ignore@", targetFile, DataType.ASCII));

        send(sftpServer)
                .message(FtpMessage.success());

        sleep(1000L);

        echo("Retrieve file from server");

        Path downloadTarget = Paths.get(downloadPath);
        Files.createDirectories(downloadTarget.getParent());

        async().actions(new ScpClientAction(sftpServer.getUser(), "localhost", sftpServer.getPort(), privateKey.getFile().getAbsolutePath())
                                .download(targetFile, Paths.get(downloadPath).toAbsolutePath().toString()));

        receive(sftpServer)
                .message(FtpMessage.get("/" + targetFile, "@ignore@", DataType.ASCII));

        send(sftpServer)
                .message(FtpMessage.success());

        sleep(1000);
        
        action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                try {
                    String content = FileUtils.readToString(new FileInputStream(Paths.get(downloadPath).toFile()));
                    Assert.assertEquals(content, FileUtils.readToString(FileUtils.getFileResource(sourcePath)));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read downloaded file", e);
                }
            }
        });
    }
}
