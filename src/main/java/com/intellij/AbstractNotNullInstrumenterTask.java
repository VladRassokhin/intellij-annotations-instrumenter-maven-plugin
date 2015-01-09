/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import se.eris.asm.AsmUtils;
import se.eris.util.ClassFileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractNotNullInstrumenterTask extends AbstractMojo {

    @SuppressWarnings("UnusedDeclaration")
    @Component
    protected MavenProject project;

    protected void instrument(@NotNull final String classesDirectory, @NotNull final List<String> classpathElements) throws MojoExecutionException {
        final List<URL> urls = new ArrayList<URL>();
        try {
            for (final String cp : classpathElements) {
                urls.add(new File(cp).toURI().toURL());
            }
        } catch (final MalformedURLException e) {
            throw new MojoExecutionException("Cannot convert classpath element into URL", e);
        }
        final InstrumentationClassFinder finder = new InstrumentationClassFinder(urls.toArray(new URL[urls.size()]));
        final int instrumented = instrumentDirectoryRecursive(new File(classesDirectory), finder);
        getLog().info("Added @NotNull assertions to " + instrumented + " files");
    }

    private int instrumentDirectoryRecursive(@NotNull final File classesDirectory, @NotNull final InstrumentationClassFinder finder) throws MojoExecutionException {
        int instrumentedCounter = 0;
        final Collection<File> classes = ClassFileUtils.getClassFiles(classesDirectory.toPath());
        for (@NotNull final File file : classes) {
            getLog().debug("Adding @NotNull assertions to " + file.getPath());
            try {
                instrumentedCounter += instrumentClass(file, finder) ? 1 : 0;
            } catch (final IOException e) {
                getLog().warn("Failed to instrument @NotNull assertion for " + file.getPath() + ": " + e.getMessage());
            } catch (final RuntimeException e) {
                throw new MojoExecutionException("@NotNull instrumentation failed for " + file.getPath() + ": " + e.toString(), e);
            }
        }
        return instrumentedCounter;
    }

    private boolean instrumentClass(@NotNull final File file, @NotNull final InstrumentationClassFinder finder) throws java.io.IOException {
        final FileInputStream inputStream = new FileInputStream(file);
        try {
            final ClassReader classReader = new ClassReader(inputStream);

            final int fileVersion = getClassFileVersion(classReader);

            if (javaVersionSupportsAnnotations(fileVersion)) {
                final ClassWriter writer = new InstrumenterClassWriter(getAsmClassWriterFlags(fileVersion), finder);

                final NotNullVerifyingInstrumenter instrumenter = new NotNullVerifyingInstrumenter(writer, new HashSet<String>());
                classReader.accept(instrumenter, 0);
                if (instrumenter.isModification()) {
                    final FileOutputStream fileOutputStream = new FileOutputStream(file);
                    try {
                        fileOutputStream.write(writer.toByteArray());
                        return true;
                    } finally {
                        fileOutputStream.close();
                    }
                }
            }
        } finally {
            inputStream.close();
        }
        return false;
    }

    private boolean javaVersionSupportsAnnotations(final int version) {
        return AsmUtils.asmOpcodeToJavaVersion(version) >= 5;
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
