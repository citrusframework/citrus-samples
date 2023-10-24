package com.consol.citrus.samples.todolist;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ClearUserHomeDirAction extends AbstractTestAction {

    private final String userHomePath;

    /**
     * Constructor initializing target file path.
     * @param userHomePath
     */
    public ClearUserHomeDirAction(String userHomePath) {
        this.userHomePath = userHomePath;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (Files.exists(Paths.get(new FileSystemResource(userHomePath).getURI()))) {
                Files.walkFileTree(Paths.get(new FileSystemResource(userHomePath).getURI()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }
                });

                Files.deleteIfExists(Paths.get(new FileSystemResource(userHomePath).getURI()));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to delete user home directory", e);
        }
    }

    /**
     * Gets the userHomePath.
     *
     * @return
     */
    public String getUserHomePath() {
        return userHomePath;
    }
}
