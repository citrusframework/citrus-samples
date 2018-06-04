package com.consol.citrus.samples.todolist;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Christoph Deppisch
 */
public class ScpClientAction extends AbstractTestAction {

    private final String user;
    private final String host;
    private final int port;
    private final Resource privateKeyResource;

    private Resource source;
    private String sourcePath;

    private String targetPath;

    private Mode mode = Mode.UPLOAD;

    public enum Mode { DOWNLOAD, UPLOAD }

    /**
     * Default constructor using fields.
     * @param user
     * @param host
     * @param port
     * @param privateKeyResource
     */
    public ScpClientAction(String user, String host, int port, Resource privateKeyResource) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.privateKeyResource = privateKeyResource;
    }

    @Override
    public void doExecute(TestContext context) {
        try (BufferedReader stdin = new BufferedReader(new InputStreamReader(new NoCloseInputStream(System.in)))) {
            ClientSession session = SshClient.setupClientSession("-P", stdin, System.out, System.err, "-P", String.valueOf(port), "-o", "HostKeyAlgorithms=+ssh-dss", "-i", privateKeyResource.getFile().getAbsolutePath(), "-l", user, host);
            ScpClient scpClient = session.createScpClient();

            if (mode.equals(Mode.DOWNLOAD)) {
                Files.createDirectories(Paths.get(targetPath).getParent());
                scpClient.download(sourcePath, Paths.get(targetPath).toAbsolutePath().toString());
            } else if (mode.equals(Mode.UPLOAD)) {
                scpClient.upload(source.getFile().getAbsolutePath(), targetPath);
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to %s file via SCP", mode.name().toLowerCase()), e);
        }
    }

    /**
     * Gets the mode.
     *
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets the source.
     *
     * @return
     */
    public Resource getSource() {
        return source;
    }

    /**
     * Sets the source.
     *
     * @param source
     */
    public void setSource(Resource source) {
        this.source = source;
    }

    /**
     * Gets the sourcePath.
     *
     * @return
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets the sourcePath.
     *
     * @param sourcePath
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Gets the targetPath.
     *
     * @return
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     * Sets the targetPath.
     *
     * @param targetPath
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
}
