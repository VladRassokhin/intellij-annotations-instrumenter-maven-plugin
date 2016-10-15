/*
 * Copyright 2013-2015 Eris IT AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public static Set<File> getClassFiles(@NotNull final Path rootDir) {
        try {
            final ClassFileCollector collector = new ClassFileCollector();
            Files.walkFileTree(rootDir, collector);
            return collector.getClassFiles();
        } catch (final IOException e) {
            throw new RuntimeException("Could not collect class files in directory '" + rootDir + "'", e);
        }
    }

    private static class ClassFileCollector extends SimpleFileVisitor<Path> {
        private static final String CLASS_FILE_EXTENSION = ".class";

        private final Set<File> classFiles = new HashSet<>();

        @Override
        public FileVisitResult visitFile(@NotNull final Path path, @NotNull final BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && path.toFile().getName().endsWith(CLASS_FILE_EXTENSION)) {
                classFiles.add(path.toFile());
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(@NotNull final Path file, @NotNull final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        private Set<File> getClassFiles() {
            return classFiles;
        }
    }

}
