FTP sample ![Logo][1]
==============

This sample shows hot to setup proper FTP communication as client and server in Citrus. The ftp-client component creates a new
directory on the server, stores a file and retrieves the same file in a single test.

FTP features are also described in detail in [reference guide][4]

Objectives
---------

We want to setup both FTP client and server components in our project in order to test a file transfer via FTP. The client authenticates to the server
using username password credentials. The ftp-server component will receive incoming requests for validation and provide the FTP user account workspace to the client.

First of all let us setup the necessary components in the Spring bean configuration:

```java
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
```

The *ftpServer* is a small but fully qualified FTP server implementation in Citrus. The server receives `user-manager-properties` that define all available user accounts. The properties
look like this:

```properties
# Password is "admin"
ftpserver.user.citrus.userpassword=21232F297A57A5A743894A0E4A801FC3
ftpserver.user.citrus.homedirectory=target/ftp/user/citrus
ftpserver.user.citrus.enableflag=true
ftpserver.user.citrus.writepermission=true
ftpserver.user.citrus.maxloginnumber=0
ftpserver.user.citrus.maxloginperip=0
ftpserver.user.citrus.idletime=0
ftpserver.user.citrus.uploadrate=0
ftpserver.user.citrus.downloadrate=0

ftpserver.user.anonymous.userpassword=
ftpserver.user.anonymous.homedirectory=target/ftp/user/anonymous
ftpserver.user.anonymous.enableflag=true
ftpserver.user.anonymous.writepermission=false
ftpserver.user.anonymous.maxloginnumber=20
ftpserver.user.anonymous.maxloginperip=2
ftpserver.user.anonymous.idletime=300
ftpserver.user.anonymous.uploadrate=4800
ftpserver.user.anonymous.downloadrate=4800
```

The FTP server defines two accounts `citrus` and `anonymous`. Clients may authenticate to the server using these credentials. Based on the user account
we can set a user workspace home directory. The server will save incoming stored files to this directory and the server will read retrieved files from that
home directory.

In case you want to setup some files in that directory in order to provide it to clients, please copy those files to that home directory prior to the test.  

The ftp-client connects to the server using the user credentials and is then able to store and retrieve files in a test.

In a sample test we first create a new subdirectory in that user home directory.

```java
echo("Create new directory on server");

send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(ftpClient)
    .message(FtpMessage.command(FTPCmd.MKD).arguments("todo")));

receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(ftpClient)
    .message(FtpMessage.result(getMkdirsCommandResult("todo"))));
```

As you can see the client is passing a `MKD` signal to the server. The user login procedure is done automatically and the directory creation is also
dome automatically on the FTP server. This is because we added the `MKD` signal to the list of `auto-handle-commands`.

```java
@Bean
public FtpServer ftpListServer() {
    return CitrusEndpoints
        .ftp()
            .server()
            [...]
               .autoLogin(true)
               .autoStart(true)
               .autoHandleCommands(Stream.of(FTPCmd.MKD.getCommand(),
                                             FTPCmd.PORT.getCommand(),
                                             FTPCmd.PASV.getCommand(),
                                             FTPCmd.TYPE.getCommand()).collect(Collectors.joining(",")))
            [...]
            .build();
}
```

This tells the FTP server to automatically handle the user login as well as the given commands. Now lets store a new file in that user directory.

```java
echo("Store file to directory");

send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(ftpClient)
    .fork(true)
    .message(FtpMessage.put("classpath:todo/entry.json", "todo/todo.json", DataType.ASCII)));

receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(ftpServer)
    .message(FtpMessage.command(FTPCmd.STOR).arguments("todo/todo.json")));

send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(ftpServer)
    .message(FtpMessage.success()));

receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(ftpClient)
    .message(FtpMessage.result(getStoreFileCommandResult())));
```

Now we have both client and server interaction in the same test case. This requires us to use `fork=true` option on all client
requests as we need to continue with the test in order to handle the server interaction, too. We can store a new file `todo/entry.json` which is transmitted
to the server using `ASCII` file mode.

The FTP server is receiving the `STOR`signal providing a success response in order to mark completion of the file transfer. After that the file should be created in
the user home directory in path `todo/entry.json`. You can validate the file content by reading it from that directory in another test action.

Now we should be also able to list the files in that directory:

```java
echo("List files in directory");

send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(ftpClient)
    .fork(true)
    .message(FtpMessage.list("todo")));

receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(ftpServer)
    .message(FtpMessage.command(FTPCmd.LIST).arguments("todo")));

send(sendMessageBuilder -> sendMessageBuilder
    .endpoint(ftpServer)
    .message(FtpMessage.success()));

receive(receiveMessageBuilder -> receiveMessageBuilder
    .endpoint(ftpClient)
    .message(FtpMessage.result(getListCommandResult("todo.json"))));
```

```java
private ListCommandResult getListCommandResult(String ... fileNames) {
    ListCommandResult result = new ListCommandResult();
    result.setSuccess(true);
    result.setReplyCode(String.valueOf(226));
    result.setReplyString("@contains('Closing data connection')@");

    ListCommandResult.Files expectedFiles = new ListCommandResult.Files();

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

    send(sendMessageBuilder -> sendMessageBuilder
        .endpoint(ftpClient)
        .fork(true)
        .message(FtpMessage.get("todo/todo.json", "target/todo/todo.json", DataType.ASCII)));

    receive(receiveMessageBuilder -> receiveMessageBuilder
        .endpoint(ftpServer)
        .message(FtpMessage.command(FTPCmd.RETR).arguments("todo/todo.json")));

    send(sendMessageBuilder -> sendMessageBuilder
        .endpoint(ftpServer)
        .message(FtpMessage.success()));

    receive(receiveMessageBuilder -> receiveMessageBuilder
        .endpoint(ftpClient)
        .message(FtpMessage.result(getRetrieveFileCommandResult("target/todo/todo.json", new ClassPathResource("todo/entry.json")))));
```

```java
private GetCommandResult getRetrieveFileCommandResult(String path, Resource content) {
    // TODO: mbu
    // We should take a look at this. Since we use this method within a lambda, it must be pure,
    // Whether the presented solution is "the way to go" is up for debate.
    GetCommandResult result = new GetCommandResult();
    try {
        result.setSuccess(true);
        result.setReplyCode(String.valueOf(226));
        result.setReplyString("@contains('Transfer complete')@");

        GetCommandResult.File entryResult = new GetCommandResult.File();
        entryResult.setPath(path);
        entryResult.setData(FileUtils.readToString(content));
        result.setFile(entryResult);
    } catch (IOException e) {
        log.error(e.toString());
    }

    return result;
}
```

This completes our test as we were able to interact with the FTP server using the client signals.

Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dsystem.under.test.mode=embedded
    
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

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#ftp
