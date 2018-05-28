package com.consol.citrus.samples.todolist;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class DeleteFtpFilesAction extends AbstractTestAction {

    private String ftpDirectoryPath;

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

    /**
     * Sets the ftpDirectoryPath.
     *
     * @param ftpDirectoryPath
     */
    public void setFtpDirectoryPath(String ftpDirectoryPath) {
        this.ftpDirectoryPath = ftpDirectoryPath;
    }
}
