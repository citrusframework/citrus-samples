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

```xml
<citrus-sftp:client id="sftpClient"
                   strict-host-checking="false"
                   port="2222"
                   username="citrus"
                   private-key-path="classpath:ssh/citrus.priv"
                   timeout="10000"/>

<citrus-sftp:server id="sftpServer"
                   port="2222"
                   auto-start="true"
                   user="citrus"
                   password="admin"
                   allowed-key-path="classpath:ssh/citrus_pub.pem"/>
```

The *sftpServer* is a small but fully qualified SFTP server implementation in Citrus. The server receives a `user` that defines the user account and its home directory. All commands
will be performed in this user home directory. You can set the user home directory using the `user-home-path` attribute on the server. By default this is a directory located in `${user.dir}/target/{serverName}/home/{user}`. 

In case you want to setup some files in that directory in order to provide it to clients, please copy those files to that home directory prior to the test.  

The sftp-client connects to the server using the user credentials and/or the private key authentication. The client uses the private key where the server adds the public key to the list of allowed keys.

In a sample test we first create a new subdirectory in that user home directory.

```xml
<echo>
  <message>Create new directory on server</message>
</echo>

<send endpoint="sftpClient">
  <message>
    <payload>
      <ftp:command>
        <ftp:signal>MKD</ftp:signal>
        <ftp:arguments>todo</ftp:arguments>
      </ftp:command>
    </payload>
  </message>
</send>

<receive endpoint="sftpClient">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>257</ftp:reply-code>
        <ftp:reply-string>Pathname created</ftp:reply-string>
      </ftp:command-result>
    </payload>
  </message>
</receive>
```

As you can see the client is passing a `MKD` signal to the server. The user login procedure is done automatically and the directory creation is also
dome automatically on the SFTP server. This is because the test case is not able to intercept those commands such as MKD and LIST on the server. The commands are directly
executed in the user home directory. 

Now lets store a new file in that user directory.

```xml
<echo>
  <message>Store file to directory</message>
</echo>

<send endpoint="sftpClient" fork="true">
  <message>
    <payload>
      <ftp:put-command>
        <ftp:file path="classpath:todo/entry.json" type="ASCII"/>
        <ftp:target path="todo/todo.json"/>
      </ftp:put-command>
    </payload>
  </message>
</send>

<receive endpoint="sftpServer">
  <message>
    <payload>
      <ftp:put-command>
        <ftp:signal>STOR</ftp:signal>
        <ftp:file path="@ignore@" type="ASCII"/>
        <ftp:target path="/todo/todo.json"/>
      </ftp:put-command>
    </payload>
  </message>
</receive>

<send endpoint="sftpServer">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
      </ftp:command-result>
    </payload>
  </message>
</send>

<receive endpoint="sftpClient">
  <message>
    <payload>
      <ftp:put-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>226</ftp:reply-code>
        <ftp:reply-string>Transfer complete</ftp:reply-string>
      </ftp:put-command-result>
    </payload>
  </message>
</receive>
```

Now we have both client and server interaction in the same test case. This requires us to use `fork=true` option on all client
requests as we need to continue with the test in order to handle the server interaction, too. We can store a new file `todo/entry.json` which is transmitted
to the server using `ASCII` file mode.

The FTP server is receiving the `STOR`signal providing a success response in order to mark completion of the file transfer. After that the file should be created in
the user home directory in path `todo/todo.json`. You can validate the file content by reading it from that directory in another test action.

Now we should be also able to list the files in that directory:

```xml
<echo>
  <message>List files in directory</message>
</echo>

<send endpoint="sftpClient">
  <message>
    <payload>
      <ftp:list-command>
        <ftp:target path="todo" />
      </ftp:list-command>
    </payload>
  </message>
</send>

<receive endpoint="sftpClient">
  <message>
    <payload>
      <ftp:list-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>150</ftp:reply-code>
        <ftp:reply-string>List files complete</ftp:reply-string>
        <ftp:files>
          <ftp:file path="."/>
          <ftp:file path=".."/>
          <ftp:file path="todo.json"/>
        </ftp:files>
      </ftp:list-command-result>
    </payload>
  </message>
</receive>
```

Now we can also retrieve the file from the server by calling the `RETR` operation.

```xml
<echo>
  <message>Retrieve file from server</message>
</echo>

<send endpoint="sftpClient" fork="true">
  <message>
    <payload>
      <ftp:get-command>
        <ftp:file path="todo/todo.json" type="ASCII"/>
        <ftp:target path="target/todo/todo.json"/>
      </ftp:get-command>
    </payload>
  </message>
</send>

<receive endpoint="sftpServer">
  <message>
    <payload>
      <ftp:get-command>
        <ftp:signal>RETR</ftp:signal>
        <ftp:file path="/todo/todo.json" type="ASCII"/>
        <ftp:target path="@ignore@"/>
      </ftp:get-command>
    </payload>
  </message>
</receive>

<send endpoint="sftpServer">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
      </ftp:command-result>
    </payload>
  </message>
</send>

<receive endpoint="sftpClient">
  <message>
    <payload>
      <ftp:get-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>226</ftp:reply-code>
        <ftp:reply-string>Transfer complete</ftp:reply-string>
        <ftp:file path="target/todo/todo.json">
          <ftp:data>citrus:readFile('classpath:todo/entry.json')</ftp:data>
        </ftp:file>
      </ftp:get-command-result>
    </payload>
  </message>
</receive>
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
