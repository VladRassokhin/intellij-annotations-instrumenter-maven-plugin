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
import com.intellij.compiler.notNullVerification.NotNullVerifyingInstrumenter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import se.eris.asm.AsmUtils;
import se.eris.maven.LogWrapper;
import se.eris.notnull.InstrumenterExecutionException;
import se.eris.util.ClassFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Olle Sundblad
 */
public class NotNullInstrumenter {

    @NotNull
    private final LogWrapper logger;

    public NotNullInstrumenter(@NotNull final LogWrapper logWrapper) {
        logger = logWrapper;
    }

    public int addNotNullAnnotations(@NotNull final String classesDirectory, @NotNull final Set<String> notNullAnnotations,@NotNull final  List<URL> urls) {
        final InstrumentationClassFinder finder = new InstrumentationClassFinder(urls.toArray(new URL[urls.size()]));
        return instrumentDirectoryRecursive(new File(classesDirectory), finder, notNullAnnotations);
    }

    private int instrumentDirectoryRecursive(@NotNull final File classesDirectory, @NotNull final InstrumentationClassFinder finder, @NotNull final Set<String> notNullAnnotations) {
        int instrumentedCounter = 0;
        final Collection<File> classes = ClassFileUtils.getClassFiles(classesDirectory.toPath());
        for (@NotNull final File file : classes) {
            instrumentedCounter += instrumentFile(file, finder, notNullAnnotations);
        }
        return instrumentedCounter;
    }

    private int instrumentFile(@NotNull final File file, @NotNull final InstrumentationClassFinder finder, @NotNull final Set<String> notNullAnnotations)  {
        logger.debug("Adding @NotNull assertions to " + file.getPath());
        try {
            return instrumentClass(file, finder, notNullAnnotations) ? 1 : 0;
        } catch (final IOException e) {
            logger.warn("Failed to instrument @NotNull assertion for " + file.getPath() + ": " + e.getMessage());
        } catch (final RuntimeException e) {
            throw new InstrumenterExecutionException("@NotNull instrumentation failed for " + file.getPath() + ": " + e.toString(), e);
        }
        return 0;
    }

    private static boolean instrumentClass(@NotNull final File file, @NotNull final InstrumentationClassFinder finder, @NotNull final Set<String> notNullAnnotations) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            final ClassReader classReader = new ClassReader(inputStream);

            final int fileVersion = getClassFileVersion(classReader);

            if (AsmUtils.javaVersionSupportsAnnotations(fileVersion)) {
                final ClassWriter writer = new InstrumenterClassWriter(getAsmClassWriterFlags(fileVersion), finder);

                final NotNullVerifyingInstrumenter instrumenter = new NotNullVerifyingInstrumenter(writer, notNullAnnotations);
                classReader.accept(instrumenter, 0);
                if (instrumenter.isModification()) {
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
        return AsmUtils.asmOpcodeToJavaVersion(version) >= 6 ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS;
    }

    private static int getClassFileVersion(@NotNull final ClassReader reader) {
        final int[] classFileVersion = new int[1];
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                classFileVersion[0] = version;
            }
        }, 0);
        return classFileVersion[0];
    }
}
