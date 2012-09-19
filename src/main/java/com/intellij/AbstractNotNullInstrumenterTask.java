/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractNotNullInstrumenterTask extends org.apache.maven.plugin.AbstractMojo {
    @Component
    protected org.apache.maven.project.MavenProject project;

    protected void instrument(@NotNull final String directory, @NotNull final List<String> classpathElements) throws MojoExecutionException {
        final ArrayList<URL> urls = new ArrayList<URL>();
        try {
            for (String cp : classpathElements) {
                urls.add(new File(cp).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Cannot convert classpath element into URL", e);
        }
        final InstrumentationClassFinder finder = new InstrumentationClassFinder(urls.toArray(new URL[urls.size()]));
        instrumentDirectoryRecursive(new File(directory), finder);
    }

    private int instrumentDirectoryRecursive(@NotNull final File dir, @NotNull final InstrumentationClassFinder finder) throws MojoExecutionException {
        int instrumented = 0;
        final Collection<File> classes = collectClasses(dir);
        for (@NotNull final File file : classes) {
            getLog().info("Adding @NotNull assertions to " + file.getPath());
            try {
                instrumented += instrumentClass(file, finder) ? 1 : 0;
            } catch (IOException e) {
                getLog().warn("Failed to instrument @NotNull assertion for " + file.getPath() + ": " + e.getMessage());
            } catch (Exception e) {
                throw new MojoExecutionException("@NotNull instrumentation failed for " + file.getPath() + ": " + e.toString(), e);
            }
        }
        return instrumented;
    }

    private boolean instrumentClass(@NotNull final File file, @NotNull final InstrumentationClassFinder finder) throws java.io.IOException {
        final FileInputStream inputStream = new FileInputStream(file);
        try {
            ClassReader reader = new ClassReader(inputStream);

            int version = getClassFileVersion(reader);

            if (version != Opcodes.V1_1 && version >= Opcodes.V1_5) {
                ClassWriter writer = new InstrumenterClassWriter(getAsmClassWriterFlags(version), finder);

                final NotNullVerifyingInstrumenter instrumenter = new NotNullVerifyingInstrumenter(writer);
                reader.accept(instrumenter, 0);
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

    @NotNull
    private static Collection<File> collectClasses(@NotNull final File directory) {
        final HashSet<File> classes = new HashSet<File>();
        final Queue<File> queue = new LinkedList<File>();
        queue.add(directory);
        while (!queue.isEmpty()) {
            File dir = queue.poll();
            File[] files = dir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.isFile() && file.getName().endsWith(".class")) {
                    classes.add(file);
                }
            }
        }
        return classes;
    }

    /**
     * @return the flags for class writer
     */
    private static int getAsmClassWriterFlags(int version) {
        return version >= Opcodes.V1_6 && version != Opcodes.V1_1 ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS;
    }

    private static int getClassFileVersion(@NotNull final ClassReader reader) {
        final int[] classfileVersion = new int[1];
        reader.accept(new ClassVisitor(Opcodes.ASM4) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                classfileVersion[0] = version;
            }
        }, 0);
        return classfileVersion[0];
    }
}
