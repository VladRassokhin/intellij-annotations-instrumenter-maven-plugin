package se.eris.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class ClassFileUtils {

    @NotNull
    public static Set<File> collectClassFiles(@NotNull final Path start) {
        try {
            final ClassFileCollector collector = new ClassFileCollector();
            Files.walkFileTree(start, collector);
            return collector.getClassFiles();
        } catch (final IOException e) {
            throw new RuntimeException("Could not collect class files for directory '" + start + "'", e);
        }
    }

    private static class ClassFileCollector extends SimpleFileVisitor<Path> {
        private final Set<File> classFiles = new HashSet<File>();

        @Override
        public FileVisitResult visitFile(@NotNull final Path path, @NotNull final BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && path.toFile().getName().endsWith(".class")) {
                classFiles.add(path.toFile());
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(@NotNull final Path file, @NotNull final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        public Set<File> getClassFiles() {
            return classFiles;
        }
    }
}
