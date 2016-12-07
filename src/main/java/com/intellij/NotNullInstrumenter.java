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
package com.intellij;

import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import com.intellij.compiler.instrumentation.InstrumenterClassWriter;
import com.intellij.compiler.notNullVerification.NotNullInstrumenterClassVisitor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import se.eris.asm.AsmUtils;
import se.eris.maven.LogWrapper;
import se.eris.notnull.InstrumenterExecutionException;
import se.eris.notnull.NotNullConfiguration;
import se.eris.util.ClassFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * @author Olle Sundblad
 */
public class NotNullInstrumenter {

    private static final int NO_FLAGS = 0;

    @NotNull
    private final LogWrapper logger;

    public NotNullInstrumenter(@NotNull final LogWrapper logWrapper) {
        logger = logWrapper;
    }

    public int addNotNullAnnotations(@NotNull final String classesDirectory, final NotNullConfiguration configuration, @NotNull final List<URL> urls) {
        final InstrumentationClassFinder finder = new InstrumentationClassFinder(urls.toArray(new URL[urls.size()]));
        return instrumentDirectoryRecursive(new File(classesDirectory), finder, configuration);
    }

    private int instrumentDirectoryRecursive(@NotNull final File classesDirectory, @NotNull final InstrumentationClassFinder finder, final NotNullConfiguration configuration) {
        int instrumentedCounter = 0;
        final Collection<File> classes = ClassFileUtils.getClassFiles(classesDirectory.toPath());
        for (@NotNull final File file : classes) {
            instrumentedCounter += instrumentFile(file, finder, configuration);
        }
        return instrumentedCounter;
    }

    private int instrumentFile(@NotNull final File file, @NotNull final InstrumentationClassFinder finder, final NotNullConfiguration configuration) {
        logger.debug("Adding NotNull assertions to " + file.getPath());
        try {
            return instrumentClass(file, finder, configuration) ? 1 : 0;
        } catch (final IOException e) {
            logger.warn("Failed to instrument NotNull assertion for " + file.getPath() + ": " + e.getMessage());
        } catch (final RuntimeException e) {
            throw new InstrumenterExecutionException("NotNull instrumentation failed for " + file.getPath() + ": " + e.toString(), e);
        }
        return 0;
    }

    private static boolean instrumentClass(@NotNull final File file, @NotNull final InstrumentationClassFinder finder, final NotNullConfiguration configuration) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            final ClassReader classReader = new ClassReader(inputStream);

            final int fileVersion = getClassFileVersion(classReader);

            if (AsmUtils.javaVersionSupportsAnnotations(fileVersion)) {
                final ClassWriter writer = new InstrumenterClassWriter(getAsmClassWriterFlags(fileVersion), finder);

                final NotNullInstrumenterClassVisitor instrumentingVisitor = new NotNullInstrumenterClassVisitor(writer, configuration);
                classReader.accept(instrumentingVisitor, NO_FLAGS);
                if (instrumentingVisitor.hasInstrumented()) {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(writer.toByteArray());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return the flags for class writer
     */
    private static int getAsmClassWriterFlags(final int version) {
        return (AsmUtils.asmOpcodeToJavaVersion(version) >= AsmUtils.JAVA_VERSION_6) ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS;
    }

    private static int getClassFileVersion(@NotNull final ClassReader reader) {
        final int[] classFileVersion = new int[1];
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                classFileVersion[0] = version;
            }
        }, NO_FLAGS);
        return classFileVersion[0];
    }
}
