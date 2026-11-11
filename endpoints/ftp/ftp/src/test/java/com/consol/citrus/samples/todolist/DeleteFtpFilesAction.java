package com.consol.citrus.samples.todolist;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class DeleteFtpFilesAction extends AbstractTestAction {

    private final String ftpDirectoryPath;

    /**
     * Constructor initializing target file path.
     * @param ftpDirectoryPath
     */
    public DeleteFtpFilesAction(String ftpDirectoryPath) {
        this.ftpDirectoryPath = ftpDirectoryPath;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (Files.exists(Paths.get(new FileSystemResource(ftpDirectoryPath).getURI()))) {
                Files.walkFileTree(Paths.get(new FileSystemResource(ftpDirectoryPath).getURI()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }
                });

                Files.deleteIfExists(Paths.get(new FileSystemResource(ftpDirectoryPath).getURI()));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to delete ftp user directory", e);
        }
    }

    /**
     * Gets the ftpDirectoryPath.
     *
     * @return
     */
    public String getFtpDirectoryPath() {
        return ftpDirectoryPath;
    }
}
