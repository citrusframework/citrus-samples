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

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.DataType;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.ftp.client.SftpClient;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.GetCommandResult;
import org.citrusframework.ftp.model.ListCommandResult;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private SftpClient sftpClient;

    @Autowired
    private SftpServer sftpServer;

    // TODO: enable when Citrus 4.1.1 is available
    @Test(enabled = false)
    @CitrusTest
    public void testStoreAndRetrieveFile() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(echo("Remove ftp user directory if present"));

        $(new ClearUserHomeDirAction(String.format("target/%s/home/%s/todo", sftpServer.getName(), sftpServer.getUser())));

        $(echo("Create new directory on server"));

        $(send()
            .endpoint(sftpClient)
            .message(FtpMessage.command(FTPCmd.MKD).arguments("todo")));

        $(receive()
            .endpoint(sftpClient)
            .message(FtpMessage.success(257, "Pathname created")));

        $(echo("Directory 'todo' created on SFTP server"));
        $(echo("Store file to directory"));

        $(send()
            .endpoint(sftpClient)
            .fork(true)
            .message(FtpMessage.put("classpath:todo/entry.json", "todo/todo.json", DataType.ASCII)));

        $(receive()
            .endpoint(sftpServer)
            .message(FtpMessage.put("@ignore@", "/todo/todo.json", DataType.ASCII)));

        $(send()
            .endpoint(sftpServer)
            .message(FtpMessage.success()));

        $(receive()
            .endpoint(sftpClient)
            .message(FtpMessage.putResult(226, "@contains(Transfer complete)@", true)));

        $(echo("List files in directory"));

        $(send()
            .endpoint(sftpClient)
            .message(FtpMessage.list("todo")));

        $(receive()
            .endpoint(sftpClient)
            .message(FtpMessage.result(getListCommandResult("todo.json"))));

        $(echo("Retrieve file from server"));

        $(send()
            .endpoint(sftpClient)
            .fork(true)
            .message(FtpMessage.get("todo/todo.json", "target/todo/todo.json", DataType.ASCII)));

        $(receive()
            .endpoint(sftpServer)
            .message(FtpMessage.get("/todo/todo.json", "@ignore@", DataType.ASCII)));

        $(send()
            .endpoint(sftpServer)
            .message(FtpMessage.success()));

        try {
            FtpMessage fileCommandResult = FtpMessage.result(
                    getRetrieveFileCommandResult("target/todo/todo.json", Resources.fromClasspath("todo/entry.json")));

            $(receive().endpoint(sftpClient)
                    .message(fileCommandResult));
        } catch (IOException e) {
            $(fail(e.getMessage()));
        }
    }

    private ListCommandResult getListCommandResult(String ... fileNames) {
        ListCommandResult result = new ListCommandResult();
        result.setSuccess(true);
        result.setReplyCode(String.valueOf(150));
        result.setReplyString("List files complete");

        ListCommandResult.Files expectedFiles = new ListCommandResult.Files();

        ListCommandResult.Files.File currentDir = new ListCommandResult.Files.File();
        currentDir.setPath(".");
        expectedFiles.getFiles().add(currentDir);

        ListCommandResult.Files.File parentDir = new ListCommandResult.Files.File();
        parentDir.setPath("..");
        expectedFiles.getFiles().add(parentDir);

        for (String fileName : fileNames) {
            ListCommandResult.Files.File entry = new ListCommandResult.Files.File();
            entry.setPath(fileName);
            expectedFiles.getFiles().add(entry);
        }

        result.setFiles(expectedFiles);

        return result;
    }

    private GetCommandResult getRetrieveFileCommandResult(String path, Resource content) throws IOException {
        GetCommandResult result = new GetCommandResult();
        result.setSuccess(true);
        result.setReplyCode(String.valueOf(226));
        result.setReplyString("Transfer complete");

        GetCommandResult.File entryResult = new GetCommandResult.File();
        entryResult.setPath(path);
        entryResult.setData(FileUtils.readToString(content));
        result.setFile(entryResult);

        return result;
    }
}
