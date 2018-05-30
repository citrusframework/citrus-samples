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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.ftp.client.SftpClient;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.model.GetCommandResult;
import com.consol.citrus.ftp.model.ListCommandResult;
import com.consol.citrus.ftp.server.SftpServer;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private SftpClient sftpClient;

    @Autowired
    private SftpServer sftpServer;

    @Test
    @CitrusTest
    public void testStoreAndRetrieveFile() throws IOException {
        echo("Remove ftp user directory if present");

        action(new ClearUserHomeDirAction(String.format("target/%s/home/%s/todo", sftpServer.getName(), sftpServer.getUser())));

        echo("Create new directory on server");

        send(sftpClient)
                .message(FtpMessage.command(FTPCmd.MKD).arguments("todo"));

        receive(sftpClient)
                .message(FtpMessage.success(257, "Pathname created"));

        echo("Directory 'todo' created on SFTP server");
        echo("Store file to directory");

        send(sftpClient)
            .fork(true)
            .message(FtpMessage.put("classpath:todo/entry.json", "todo/todo.json", DataType.ASCII));

        receive(sftpServer)
                .message(FtpMessage.put("@ignore@","/todo/todo.json", DataType.ASCII));

        send(sftpServer)
                .payload(FtpMessage.success().getPayload(String.class));

        receive(sftpClient)
           .message(FtpMessage.putResult(226, "@contains(Transfer complete)@", true));

        echo("List files in directory");

        send(sftpClient)
                .message(FtpMessage.list("todo"));

        receive(sftpClient)
                .message(FtpMessage.result(getListCommandResult("todo.json")));

        echo("Retrieve file from server");

        send(sftpClient)
                .fork(true)
                .message(FtpMessage.get("todo/todo.json", "target/todo/todo.json", DataType.ASCII));

        receive(sftpServer)
                .message(FtpMessage.get("/todo/todo.json", "@ignore@", DataType.ASCII));

        send(sftpServer)
                .payload(FtpMessage.success().getPayload(String.class));

        receive(sftpClient)
                .message(FtpMessage.result(getRetrieveFileCommandResult("target/todo/todo.json", new ClassPathResource("todo/entry.json"))));
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
