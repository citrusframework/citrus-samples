SCP sample ![Logo][1]
==============

This sample works with secure copy aka SCP in order to copy files as client to a server provided by Citrus. The sftp-client uses the upload for storing a new file on the 
server. After that the very same file is downloaded via SCP in a single test.

Common FTP features are also described in detail in [reference guide][4]

Objectives
---------

We want to setup both SCP client and server components in our project in order to test a file transfer via secure copy. The client authenticates to the server
using username password credentials. The secure ftp-server component will receive incoming requests for validation and provide the user account workspace to the client.

First of all let us setup the necessary components in the Spring bean configuration:

```java
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

The *sftpServer* is a small but fully qualified FTP server implementation that uses secure authentication in Citrus. The server receives an `allowedKeyPath` that defines allowed public keys. Also we define a user and password
for the test user on the server component. The user `citrus` is now able to authenticate with the server via private key. Also the client may authenticate to the server using the given username password credentials. 

Based on the user account we can set a user workspace home directory. The server will save incoming stored files to this directory and the server will read retrieved files from that
home directory.

In case you want to setup some files in that directory in order to provide it to clients, please copy those files to that home directory prior to the test.  

The sftp-client connects to the server using the user credentials and is then able to store and retrieve files in a test via SCP.

We setup a custom `ScpClientAction` as Citrus test action that will perform upload and download operations via SCP. The `ScpClientAction` receives the user, host, port and private key information. Based on that
the action is able to upload and download files to and from the SFTP server. 

```java
ClientSession session = SshClient.setupClientSession("-P", stdin, System.out, System.err, "-P", String.valueOf(port), "-o", "HostKeyAlgorithms=+ssh-dss", "-i", privateKeyPath, "-l", user, host);
ScpClient scpClient = session.createScpClient();
```

In our test we can now start to upload a file using SCP.

```java
echo("Store file via SCP");

async().actions(new ScpClientAction(sftpServer.getUser(), "localhost", sftpServer.getPort(), privateKey.getFile().getAbsolutePath())
                        .upload(new ClassPathResource("todo/entry.json").getFile().getAbsolutePath(), "todo.json"));

receive(sftpServer)
        .message(FtpMessage.put("@ignore@",targetFile, DataType.ASCII));

send(sftpServer)
        .message(FtpMessage.success());
```

Now we have both client and server interaction in the same test case. This requires us to use `async()` action container on all client
requests as we need to continue with the test in order to handle the server interaction, too. We can store a new file `todo/entry.json` which is transmitted
to the server via secure copy.

The SFTP server is receiving the file upload providing a success response in order to mark completion of the file transfer. After that the file should be created in
the user home directory as file `todo.json`. You can validate the file content by reading it from that directory in another test action.

Lets download that very same file in another SCP file transfer:

```java
echo("Retrieve file from server");

async().actions(new ScpClientAction(sftpServer.getUser(), "localhost", sftpServer.getPort(), privateKey.getFile().getAbsolutePath())
                        .download("todo.json", Paths.get("target/scp/todo.json").toAbsolutePath().toString()));

receive(sftpServer)
        .message(FtpMessage.get("/" + targetFile, "@ignore@", DataType.ASCII));

send(sftpServer)
        .message(FtpMessage.success());
```

This completes our test as we were able to interact with the SFTP server using the client secure copy operations.

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
