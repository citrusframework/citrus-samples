SFTP sample ![Logo][1]
==============

This sample shows hot to setup proper SFTP communication as client and server in Citrus. The sftp-client component uses private key authentication and creates a new
directory on the server, stores a file and retrieves the same file in a single test.

SFTP features are also described in detail in [reference guide][4]

Objectives
---------

We want to setup both FTP client and server components in our project in order to test a file transfer via FTP. The client authenticates to the server
using username password credentials. The ftp-server component will receive incoming requests for validation and provide the FTP user account workspace to the client.

First of all let us setup the necessary components in the Spring bean configuration:

```java
@Bean
public SftpClient sftpClient() {
    return CitrusEndpoints.sftp()
            .client()
            .strictHostChecking(false)
            .port(2222)
            .username("citrus")
            .privateKeyPath("classpath:ssh/citrus.priv")
            .build();
}

@Bean
public SftpServer sftpServer() {
    return CitrusEndpoints.sftp()
            .server()
            .port(2222)
            .autoStart(true)
            .user("citrus")
            .password("admin")
            .allowedKeyPath("classpath:ssh/citrus_pub.pem")
            .build();
}
```

The *sftpServer* is a small but fully qualified SFTP server implementation in Citrus. The server receives a `user` that defines the user account and its home directory. All commands
will be performed in this user home directory. You can set the user home directory using the `userHomePath` attribute on the server. By default this is a directory located in `${user.dir}/target/{serverName}/home/{user}`. 

In case you want to setup some files in that directory in order to provide it to clients, please copy those files to that home directory prior to the test.  

The sftp-client connects to the server using the user credentials and/or the private key authentication. The client uses the private key where the server adds the public key to the list of allowed keys.

In a sample test we first create a new subdirectory in that user home directory.

```java
echo("Create new directory on server");

send(sftpClient)
        .message(FtpMessage.command(FTPCmd.MKD).arguments("todo"));

receive(sftpClient)
        .message(FtpMessage.success(257, "Pathname created"));
```

As you can see the client is passing a `MKD` signal to the server. The user login procedure is done automatically and the directory creation is also
dome automatically on the SFTP server. This is because the test case is not able to intercept those commands such as MKD and LIST on the server. The commands are directly
executed in the user home directory. 

Now lets store a new file in that user directory.

```java
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
```

Now we have both client and server interaction in the same test case. This requires us to use `fork=true` option on all client
requests as we need to continue with the test in order to handle the server interaction, too. We can store a new file `todo/entry.json` which is transmitted
to the server using `ASCII` file mode.

The FTP server is receiving the `STOR`signal providing a success response in order to mark completion of the file transfer. After that the file should be created in
the user home directory in path `todo/todo.json`. You can validate the file content by reading it from that directory in another test action.

Now we should be also able to list the files in that directory:

```java
echo("List files in directory");

send(sftpClient)
        .message(FtpMessage.list("todo"));

receive(sftpClient)
        .message(FtpMessage.result(getListCommandResult("todo.json")));
```

```java
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
```

Now we can also retrieve the file from the server by calling the `RETR` operation.

```java
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
```

```java
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
```

This completes our test as we were able to interact with the SFTP server using the client signals.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dembedded
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://www.citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://www.citrusframework.org
 [3]: https://www.citrusframework.org/reference/html/
 [4]: https://www.citrusframework.org/reference/html#ftp
