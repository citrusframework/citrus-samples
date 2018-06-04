package com.consol.citrus.samples.todolist;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Christoph Deppisch
 */
public class ScpClientAction extends AbstractTestAction {

    private final String user;
    private final String host;
    private final int port;
    private final String privateKeyPath;

    private String sourcePath;
    private String targetPath;

    private Mode mode = Mode.UPLOAD;

    private enum Mode { DOWNLOAD, UPLOAD }

    /**
     * Default constructor using fields.
     * @param user
     * @param host
     * @param port
     * @param privateKeyPath
     */
    public ScpClientAction(String user, String host, int port, String privateKeyPath) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.privateKeyPath = privateKeyPath;
    }

    public ScpClientAction upload(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        mode = Mode.UPLOAD;

        return this;
    }

    public ScpClientAction download(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        mode = Mode.DOWNLOAD;

        return this;
    }

    @Override
    public void doExecute(TestContext context) {
        try (BufferedReader stdin = new BufferedReader(new InputStreamReader(new NoCloseInputStream(System.in)))) {
            ClientSession session = SshClient.setupClientSession("-P", stdin, System.out, System.err, "-P", String.valueOf(port), "-o", "HostKeyAlgorithms=+ssh-dss", "-i", privateKeyPath, "-l", user, host);
            ScpClient scpClient = session.createScpClient();

            if (mode.equals(Mode.DOWNLOAD)) {
                scpClient.download(sourcePath, targetPath);
            } else if (mode.equals(Mode.UPLOAD)) {
                scpClient.upload(sourcePath, targetPath);
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to %s file via SCP", mode.name().toLowerCase()), e);
        }
    }
}
