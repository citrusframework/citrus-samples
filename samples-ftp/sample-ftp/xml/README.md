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

```xml
<citrus-ftp:client id="ftpClient"
                   auto-read-files="true"
                   port="22222"
                   username="citrus"
                   password="admin"
                   timeout="10000"/>

<citrus-ftp:server id="ftpServer"
                   port="22222"
                   auto-start="true"
                   auto-handle-commands="MKD,PORT,TYPE"
                   user-manager-properties="classpath:citrus.ftp.user.properties"/>
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

```xml
<echo>
  <message>Create new directory on server</message>
</echo>

<send endpoint="ftpClient" fork="true">
  <message>
    <payload>
      <ftp:command>
        <ftp:signal>MKD</ftp:signal>
        <ftp:arguments>todo</ftp:arguments>
      </ftp:command>
    </payload>
  </message>
</send>

<receive endpoint="ftpClient">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>257</ftp:reply-code>
        <ftp:reply-string>257 "/todo" created.</ftp:reply-string>
      </ftp:command-result>
    </payload>
  </message>
</receive>
```

As you can see the client is passing a `MKD` signal to the server. The user login procedure is done automatically and the directory creation is also
dome automatically on the FTP server. This is because we added the `MKD` signal to the list of `auto-handle-commands`.

```xml
<citrus-ftp:server id="ftpServer"
                   [...]
                   auto-login="true"
                   auto-handle-commands="MKD,PORT,TYPE"
                   [...]/>
```

This tells the FTP server to automatically handle the user login as well as the given commands. Now lets store a new file in that user directory.

```xml
<echo>
  <message>Store file to directory</message>
</echo>

<send endpoint="ftpClient" fork="true">
  <message>
    <payload>
      <ftp:put-command>
        <ftp:file path="todo/entry.json" type="ASCII"/>
        <ftp:target path="/todo/entry.json"/>
      </ftp:put-command>
    </payload>
  </message>
</send>

<receive endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command>
        <ftp:signal>STOR</ftp:signal>
        <ftp:arguments>/todo/entry.json</ftp:arguments>
      </ftp:command>
    </payload>
  </message>
</receive>

<send endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
      </ftp:command-result>
    </payload>
  </message>
</send>

<receive endpoint="ftpClient">
  <message>
    <payload>
      <ftp:put-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>226</ftp:reply-code>
        <ftp:reply-string>@contains('Transfer complete')@</ftp:reply-string>
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

<send endpoint="ftpClient" fork="true">
  <message>
    <payload>
      <ftp:list-command>
        <ftp:target path="todo" />
      </ftp:list-command>
    </payload>
  </message>
</send>

<receive endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command>
        <ftp:signal>LIST</ftp:signal>
        <ftp:arguments>todo</ftp:arguments>
      </ftp:command>
    </payload>
  </message>
</receive>

<send endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
      </ftp:command-result>
    </payload>
  </message>
</send>

<receive endpoint="ftpClient">
  <message>
    <payload>
      <ftp:list-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>226</ftp:reply-code>
        <ftp:reply-string>@contains('Closing data connection')@</ftp:reply-string>
        <ftp:files>
          <ftp:file path="entry.json"/>
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

<send endpoint="ftpClient" fork="true">
  <message>
    <payload>
      <ftp:get-command>
        <ftp:file path="todo/todo.json" type="ASCII"/>
        <ftp:target path="target/todo/todo.json"/>
      </ftp:get-command>
    </payload>
  </message>
</send>

<receive endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command>
        <ftp:signal>RETR</ftp:signal>
        <ftp:arguments>todo/todo.json</ftp:arguments>
      </ftp:command>
    </payload>
  </message>
</receive>

<send endpoint="ftpServer">
  <message>
    <payload>
      <ftp:command-result>
        <ftp:success>true</ftp:success>
      </ftp:command-result>
    </payload>
  </message>
</send>

<receive endpoint="ftpClient">
  <message>
    <payload>
      <ftp:get-command-result>
        <ftp:success>true</ftp:success>
        <ftp:reply-code>226</ftp:reply-code>
        <ftp:reply-string>@contains('Transfer complete')@</ftp:reply-string>
        <ftp:file path="target/todo/todo.json">
          <ftp:data>citrus:readFile('classpath:todo/entry.json')</ftp:data>
        </ftp:file>
      </ftp:get-command-result>
    </payload>
  </message>
</receive>
```

This completes our test as we were able to interact with the FTP server using the client signals.

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
